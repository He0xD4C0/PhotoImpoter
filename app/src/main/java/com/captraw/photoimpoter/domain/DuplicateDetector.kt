package com.captraw.photoimpoter.domain

data class FileSignature(
    val fileName: String,
    val fileSize: Long
)

class DuplicateDetector {
    private val seen = linkedSetOf<FileSignature>()

    fun isDuplicate(fileName: String, fileSize: Long): Boolean {
        val normalized = FileSignature(fileName.lowercase(), fileSize)
        return !seen.add(normalized)
    }
}