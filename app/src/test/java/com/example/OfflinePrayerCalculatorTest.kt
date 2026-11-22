package com.example

import com.example.prayer.OfflinePrayerCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class OfflinePrayerCalculatorTest {

    @Test
    fun calculatesReasonablePrayerTimesForMionsFrance() {
        val tz = TimeZone.getTimeZone("Europe/Paris")
        val cal = Calendar.getInstance(tz).apply {
            set(2026, Calendar.NOVEMBER, 16, 17, 36, 0)
        }
        val data = OfflinePrayerCalculator.calculate(
            latitude = 45.6626,
            longitude = 4.9536,
            timezoneId = "Europe/Paris",
            methodId = 12,
            cityDisplay = "Mions Centre",
            calendar = cal,
        )

        assertEquals("Mions Centre", data.cityDisplay)
        assertEquals(5, data.entries.size)
        // Verify chronological order across the 5 prayers
        val minutes = data.entries.map { it.minutesFromMidnight }
        for (i in 0 until minutes.lastIndex) {
            assertTrue("Prayer $i should precede ${i + 1}", minutes[i] < minutes[i + 1])
        }

        val next = OfflinePrayerCalculator.computeNextPrayer(
            data = data,
            nowMillis = cal.timeInMillis,
            is24Hour = true,
        )
        assertTrue(next.remainingHours >= 0)
        assertTrue(next.remainingMinutes in 0..59)
    }
}
