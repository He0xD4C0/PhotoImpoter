package com.captraw.photoimpoter

import com.captraw.photoimpoter.domain.ExifTimestampParser
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class ExifTimestampParserTest {
    @Test
    fun parse_prefersDateTimeOriginal() {
        val result = ExifTimestampParser.parse(
            dateTimeOriginal = "2026:06:25 12:00:00",
            createDate = "2026:06:24 12:00:00",
            modifyDate = null,
            fallbackFileTime = Instant.EPOCH,
            zoneId = ZoneId.of("UTC")
        )

        assertEquals(Instant.parse("2026-06-25T12:00:00Z"), result)
    }
}