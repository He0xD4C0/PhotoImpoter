package com.captraw.photoimpoter.data

import android.net.Uri
import com.captraw.photoimpoter.domain.MediaFile

interface FileScanner {
    suspend fun scan(sourceTree: Uri): List<MediaFile>
}