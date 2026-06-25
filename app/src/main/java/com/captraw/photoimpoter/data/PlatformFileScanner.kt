package com.captraw.photoimpoter.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.captraw.photoimpoter.domain.MediaFile
import com.captraw.photoimpoter.domain.MediaFileType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.Instant
import java.util.Locale

class PlatformFileScanner(
    private var context: Context? = null,
    private val exifReader: ExifReader
) : FileScanner {
    fun setContext(context: Context) {
        this.context = context.applicationContext
    }

    override suspend fun scan(sourceTree: Uri): List<MediaFile> = withContext(Dispatchers.IO) {
        val appContext = requireNotNull(context) { "Scanner context is not initialized" }
        val root = requireNotNull(DocumentFile.fromTreeUri(appContext, sourceTree)) { "Unable to open source tree" }
        val result = mutableListOf<MediaFile>()
        walkTree(root, result)
        result
    }

    private suspend fun walkTree(directory: DocumentFile, result: MutableList<MediaFile>) {
        directory.listFiles().forEach { file ->
            if (file.isDirectory) {
                walkTree(file, result)
                return@forEach
            }
            val name = file.name ?: return@forEach
            val type = classify(name)
            if (type == MediaFileType.UNKNOWN) return@forEach
            result += MediaFile(
                uri = file.uri,
                fileName = name,
                fileSize = file.length(),
                shotAt = resolveShotTime(file.uri, file.lastModified()),
                type = type
            )
        }
    }

    private suspend fun resolveShotTime(uri: Uri, fallbackLastModified: Long): Instant {
        return runCatching { exifReader.readShotTime(uri) }
            .onFailure { Timber.w(it, "EXIF read failed for %s", uri) }
            .getOrNull() ?: Instant.ofEpochMilli(fallbackLastModified)
    }

    private fun classify(fileName: String): MediaFileType {
        val extension = fileName.substringAfterLast('.', missingDelimiterValue = "").lowercase(Locale.US)
        return when (extension) {
            "jpg", "jpeg" -> MediaFileType.JPEG
            "dng", "nef", "cr2", "cr3", "arw", "raf", "orf", "rw2" -> MediaFileType.RAW
            "xmp" -> MediaFileType.SIDECAR
            else -> MediaFileType.UNKNOWN
        }
    }
}