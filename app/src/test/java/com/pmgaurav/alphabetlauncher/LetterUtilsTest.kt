package com.pmgaurav.alphabetlauncher

import org.junit.Assert.assertEquals
import org.junit.Test

class LetterUtilsTest {

    @Test
    fun appName_returnsCorrectLetter() {

        assertEquals('W', letterFromName("WhatsApp"))
        assertEquals('Y', letterFromName("youtube"))
        assertEquals('I', letterFromName("Instagram"))
    }

    @Test
    fun appName_ignoresLeadingSpaces() {

        assertEquals(
            'W',
            letterFromName("   WhatsApp")
        )
    }

    @Test
    fun appName_nonLetter_returnsHash() {

        assertEquals(
            '#',
            letterFromName("123 App")
        )

        assertEquals(
            '#',
            letterFromName("@App")
        )
    }

    @Test
    fun names_areGroupedCorrectly() {

        val names = listOf(
            "WhatsApp",
            "youtube",
            "Instagram",
            "WhatsApp Business",
            "123 App"
        )

        val grouped = groupNamesByLetter(names)

        assertEquals(2, grouped['W']?.size)
        assertEquals(1, grouped['Y']?.size)
        assertEquals(1, grouped['I']?.size)
        assertEquals(1, grouped['#']?.size)
    }

    @Test
    fun touchPosition_mapsToCorrectLetter() {

        val height = 290

        assertEquals(
            '★',
            letterFromTouchY(0f, height)
        )

        assertEquals(
            'A',
            letterFromTouchY(15f, height)
        )

        assertEquals(
            '#',
            letterFromTouchY(270f, height)
        )

        assertEquals(
            '•',
            letterFromTouchY(289f, height)
        )
    }

    @Test
    fun invalidHeight_returnsNull() {

        assertEquals(
            null,
            letterFromTouchY(100f, 0)
        )
    }
}