package com.captraw.photoimpoter.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.captraw.photoimpoter.domain.DirectoryRule
import com.captraw.photoimpoter.domain.DuplicateDetector
import com.captraw.photoimpoter.domain.ImportMode
import com.captraw.photoimpoter.domain.ImportPlan
import com.captraw.photoimpoter.domain.ImportResult
import com.captraw.photoimpoter.domain.MediaFile
import com.captraw.photoimpoter.domain.MediaFileType
import com.captraw.photoimpoter.domain.PathGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import java.util.Locale
import kotlin.math.max

class FileImportRepository(
    private val context: Context,
    private val scanner: FileScanner,
    private val exifReader: ExifReader
) {
    suspend fun scan(sourceTree: Uri): List<MediaFile> {
        Timber.i("Scan started: %s", sourceTree)
        (scanner as? PlatformFileScanner)?.setContext(context)
        (exifReader as? ContextAwareExifReader)?.setContext(context)
        val mediaFiles = scanner.scan(sourceTree)
        Timber.i("Scan finished: %d files", mediaFiles.size)
        return mediaFiles
    }

    fun buildPlans(
        files: List<MediaFile>,
        targetRoot: Uri,
        rule: DirectoryRule,
        mode: ImportMode
    ): List<ImportPlan> {
        val detector = DuplicateDetector()
        val sidecarsByBaseName = files
            .filter { it.type == MediaFileType.SIDECAR }
            .associateBy { it.fileName.substringBeforeLast('.') }
        return buildList {
            files.forEach { file ->
                if (file.type == MediaFileType.SIDECAR) return@forEach
                val targetDirectory = PathGenerator.buildRelativeDirectory(file.shotAt, rule)
                val duplicate = detector.isDuplicate(file.fileName, file.fileSize)
                val sidecar = sidecarsByBaseName[file.fileName.substringBeforeLast('.')]
                add(
                    ImportPlan(
                        sourceFile = file,
                        targetPath = "$targetDirectory/${file.fileName}",
                        duplicate = duplicate,
                        sidecarSource = sidecar,
                        sidecarTargetPath = sidecar?.let { "$targetDirectory/${it.fileName}" }
                    )
                )
            }
        }
    }

    suspend fun importPlans(
        plans: List<ImportPlan>,
        targetRoot: Uri,
        mode: ImportMode,
        onProgress: (currentFile: String, completed: Int, total: Int, currentSpeed: String, eta: String) -> Unit = { _, _, _, _, _ -> }
    ): ImportResult = withContext(Dispatchers.IO) {
        val errors = mutableListOf<String>()
        var successCount = 0
        var duplicateCount = 0
        var failureCount = 0
        var processedBytes = 0L
        val totalBytes = plans.filterNot { it.duplicate }.sumOf { it.sourceFile.fileSize + (it.sidecarSource?.fileSize ?: 0L) }
        val rootDocument = requireNotNull(DocumentFile.fromTreeUri(context, targetRoot)) { "Unable to open target tree" }
        val startedAt = System.currentTimeMillis()

        plans.forEachIndexed { index, plan ->
            if (plan.duplicate) {
                duplicateCount++
                onProgress(plan.sourceFile.fileName, index + 1, plans.size, formatSpeed(processedBytes, startedAt), formatEta(processedBytes, totalBytes, startedAt))
                return@forEachIndexed
            }
            runCatching {
                val relativeDirectory = plan.targetPath.substringBeforeLast('/')
                val directory = ensureDirectory(rootDocument, relativeDirectory)
                val targetName = plan.sourceFile.fileName
                val mimeType = mimeTypeFor(targetName)
                val targetFile = directory.createFile(mimeType, targetName)
                    ?: throw IOException("Unable to create target file: $targetName")
                context.contentResolver.openInputStream(plan.sourceFile.uri)?.use { input ->
                    context.contentResolver.openOutputStream(targetFile.uri, "w")?.use { output ->
                        input.copyTo(output)
                    } ?: throw IOException("Unable to open output stream")
                } ?: throw IOException("Unable to open input stream")
                plan.sidecarSource?.let { sidecar ->
                    val sidecarTarget = directory.createFile(mimeTypeFor(sidecar.fileName), sidecar.fileName)
                        ?: throw IOException("Unable to create sidecar file: ${sidecar.fileName}")
                    context.contentResolver.openInputStream(sidecar.uri)?.use { input ->
                        context.contentResolver.openOutputStream(sidecarTarget.uri, "w")?.use { output ->
                            input.copyTo(output)
                        } ?: throw IOException("Unable to open sidecar output stream")
                    } ?: throw IOException("Unable to open sidecar input stream")
                }
                if (mode == ImportMode.MOVE) {
                    context.contentResolver.delete(plan.sourceFile.uri, null, null)
                    plan.sidecarSource?.let { context.contentResolver.delete(it.uri, null, null) }
                }
                successCount++
                processedBytes += plan.sourceFile.fileSize + (plan.sidecarSource?.fileSize ?: 0L)
            }.onFailure { throwable ->
                failureCount++
                val message = "${plan.sourceFile.fileName}: ${throwable.message ?: throwable::class.java.simpleName}"
                Timber.e(throwable, "File import failed: %s", message)
                errors += message
            }
            onProgress(
                plan.sourceFile.fileName,
                index + 1,
                plans.size,
                formatSpeed(processedBytes, startedAt),
                formatEta(processedBytes, totalBytes, startedAt)
            )
        }

        Timber.i("Import finished: success=%d duplicate=%d failure=%d", successCount, duplicateCount, failureCount)
        ImportResult(successCount, duplicateCount, failureCount, errors)
    }

    private fun formatSpeed(processedBytes: Long, startedAt: Long): String {
        val elapsedSeconds = max((System.currentTimeMillis() - startedAt) / 1000.0, 0.001)
        val bytesPerSecond = processedBytes / elapsedSeconds
        return "${String.format(Locale.US, "%.1f", bytesPerSecond / (1024 * 1024.0))} MB/s"
    }

    private fun formatEta(processedBytes: Long, totalBytes: Long, startedAt: Long): String {
        val remainingBytes = max(totalBytes - processedBytes, 0L)
        val elapsedSeconds = max((System.currentTimeMillis() - startedAt) / 1000.0, 0.001)
        val bytesPerSecond = processedBytes / elapsedSeconds
        if (bytesPerSecond <= 0.0) return "--"
        val seconds = (remainingBytes / bytesPerSecond).toInt()
        val minutes = seconds / 60
        val remainder = seconds % 60
        return "%dm %02ds".format(minutes, remainder)
    }

    private fun ensureDirectory(root: DocumentFile, relativeDirectory: String): DocumentFile {
        var current = root
        relativeDirectory.split('/').filter { it.isNotBlank() }.forEach { segment ->
            current = current.findFile(segment)?.takeIf { it.isDirectory }
                ?: current.createDirectory(segment)
                ?: throw IOException("Unable to create directory: $segment")
        }
        return current
    }

    private fun mimeTypeFor(fileName: String): String {
        return when (fileName.substringAfterLast('.', missingDelimiterValue = "").lowercase(Locale.US)) {
            "jpg", "jpeg" -> "image/jpeg"
            "dng" -> "image/x-adobe-dng"
            "nef" -> "image/x-nikon-nef"
            "cr2" -> "image/x-canon-cr2"
            "cr3" -> "image/x-canon-cr3"
            "arw" -> "image/x-sony-arw"
            "raf" -> "image/x-fuji-raf"
            "orf" -> "image/x-olympus-orf"
            "rw2" -> "image/x-panasonic-rw2"
            "xmp" -> "application/octet-stream"
            else -> "application/octet-stream"
        }
    }
}