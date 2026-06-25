package com.captraw.photoimpoter.domain

import java.time.ZoneId
import java.time.format.DateTimeFormatter

object PathGenerator {
    private val yyyy = DateTimeFormatter.ofPattern("yyyy")
    private val mm = DateTimeFormatter.ofPattern("MM")
    private val dd = DateTimeFormatter.ofPattern("dd")
    private val dashed = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun buildRelativeDirectory(shotAt: java.time.Instant, rule: DirectoryRule, zoneId: ZoneId = ZoneId.systemDefault()): String {
        val localDate = shotAt.atZone(zoneId)
        return when (rule) {
            DirectoryRule.YYYY_MM_DD -> listOf(localDate.format(yyyy), localDate.format(mm), localDate.format(dd)).joinToString("/")
            DirectoryRule.YYYY_MM -> listOf(localDate.format(yyyy), localDate.format(mm)).joinToString("/")
            DirectoryRule.YYYY_DASH_MM_DD -> localDate.format(dashed)
        }
    }
}