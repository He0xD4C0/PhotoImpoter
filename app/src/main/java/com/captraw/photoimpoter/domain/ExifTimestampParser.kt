package com.captraw.photoimpoter.domain

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object ExifTimestampParser {
    private val exifFormatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")

    fun parse(
        dateTimeOriginal: String?,
        createDate: String?,
        modifyDate: String?,
        fallbackFileTime: Instant,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Instant {
        return firstParsable(dateTimeOriginal, zoneId)
            ?: firstParsable(createDate, zoneId)
            ?: firstParsable(modifyDate, zoneId)
            ?: fallbackFileTime
    }

    private fun firstParsable(value: String?, zoneId: ZoneId): Instant? {
        if (value.isNullOrBlank()) return null
        return runCatching { LocalDateTime.parse(value.trim(), exifFormatter).atZone(zoneId).toInstant() }.getOrNull()
    }
}