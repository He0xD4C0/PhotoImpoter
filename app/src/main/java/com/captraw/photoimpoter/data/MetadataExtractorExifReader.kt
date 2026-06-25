package com.captraw.photoimpoter.data

import android.content.Context
import android.net.Uri
import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.Metadata
import com.drew.metadata.exif.ExifIFD0Directory
import com.drew.metadata.exif.ExifSubIFDDirectory
import com.drew.metadata.xmp.XmpDirectory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MetadataExtractorExifReader(
    private var context: Context? = null
) : ContextAwareExifReader {
    private val exifFormatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")

    override fun setContext(context: Context) {
        this.context = context.applicationContext
    }

    override suspend fun readShotTime(uri: Uri): Instant? = withContext(Dispatchers.IO) {
        val appContext = requireNotNull(context) { "Exif reader context is not initialized" }
        runCatching {
            appContext.contentResolver.openInputStream(uri)?.use { inputStream ->
                val metadata = ImageMetadataReader.readMetadata(inputStream)
                parseMetadata(metadata)
            }
        }.onFailure {
            Timber.w(it, "EXIF read failed for %s", uri)
        }.getOrNull()
    }

    private fun parseMetadata(metadata: Metadata): Instant? {
        val dateTimeOriginal = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory::class.java)
            ?.getString(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL)
        val createDate = metadata.getFirstDirectoryOfType(XmpDirectory::class.java)
            ?.xmpMeta
            ?.getPropertyString("http://ns.adobe.com/xap/1.0/", "CreateDate")
        val modifyDate = metadata.getFirstDirectoryOfType(XmpDirectory::class.java)
            ?.xmpMeta
            ?.getPropertyString("http://ns.adobe.com/xap/1.0/", "ModifyDate")
        val fallback = metadata.getFirstDirectoryOfType(ExifIFD0Directory::class.java)
            ?.getString(ExifIFD0Directory.TAG_DATETIME)
        return parseFirst(dateTimeOriginal) ?: parseFirst(createDate) ?: parseFirst(modifyDate) ?: parseFirst(fallback)
    }

    private fun parseFirst(value: String?): Instant? {
        if (value.isNullOrBlank()) return null
        return runCatching {
            LocalDateTime.parse(value.trim(), exifFormatter).atZone(ZoneId.systemDefault()).toInstant()
        }.getOrNull()
    }
}