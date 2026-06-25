package com.captraw.photoimpoter.data

import android.content.Context
import android.net.Uri

interface ExifReader {
    suspend fun readShotTime(uri: Uri): java.time.Instant?
}

interface ContextAwareExifReader : ExifReader {
    fun setContext(context: Context)
}