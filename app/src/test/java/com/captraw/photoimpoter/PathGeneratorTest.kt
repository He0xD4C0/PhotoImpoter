package com.captraw.photoimpoter

import com.captraw.photoimpoter.domain.DirectoryRule
import com.captraw.photoimpoter.domain.PathGenerator
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class PathGeneratorTest {
    @Test
    fun buildRelativeDirectory_yyyyMmDd() {
        val result = PathGenerator.buildRelativeDirectory(
            shotAt = Instant.parse("2026-06-25T10:15:30Z"),
            rule = DirectoryRule.YYYY_MM_DD,
            zoneId = ZoneId.of("UTC")
        )

        assertEquals("2026/06/25", result)
    }
}