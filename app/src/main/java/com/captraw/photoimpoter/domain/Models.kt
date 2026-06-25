package com.captraw.photoimpoter.domain

import android.net.Uri
import java.time.Instant

enum class MediaFileType {
    JPEG,
    RAW,
    SIDECAR,
    UNKNOWN
}

data class MediaFile(
    val uri: Uri,
    val fileName: String,
    val fileSize: Long,
    val shotAt: Instant,
    val type: MediaFileType
)

data class ImportPlan(
    val sourceFile: MediaFile,
    val targetPath: String,
    val duplicate: Boolean,
    val sidecarSource: MediaFile? = null,
    val sidecarTargetPath: String? = null
)

data class ImportResult(
    val successCount: Int,
    val duplicateCount: Int,
    val failureCount: Int,
    val errors: List<String>
)

enum class ImportMode {
    COPY,
    MOVE
}

enum class DirectoryRule {
    YYYY_MM_DD,
    YYYY_MM,
    YYYY_DASH_MM_DD
}