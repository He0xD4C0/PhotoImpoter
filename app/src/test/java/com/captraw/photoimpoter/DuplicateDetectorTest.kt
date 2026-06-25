package com.captraw.photoimpoter

import com.captraw.photoimpoter.domain.DuplicateDetector
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DuplicateDetectorTest {
    @Test
    fun detectsDuplicatesByNameAndSize() {
        val detector = DuplicateDetector()

        assertFalse(detector.isDuplicate("IMG_1234.NEF", 100L))
        assertTrue(detector.isDuplicate("img_1234.nef", 100L))
    }
}