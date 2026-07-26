package com.iotkin.smartoutlet.ui.screens.home

import com.iotkin.smartoutlet.data.settings.TimeFormatPreference
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeFormattingTest {

    @Test
    fun philippineTimeUsesSavedTimeFormat() {
        val value =
            "2026-07-26T17:22:12+08:00"

        assertEquals(
            "5:22:12 PM",
            formatPhilippineTime(
                value,
                TimeFormatPreference
                    .TWELVE_HOUR
            ).first
        )

        assertEquals(
            "17:22:12",
            formatPhilippineTime(
                value,
                TimeFormatPreference
                    .TWENTY_FOUR_HOUR
            ).first
        )
    }

    @Test
    fun scheduleClockUsesSavedTimeFormat() {
        assertEquals(
            "8:05 PM",
            formatScheduleClock(
                hour = 20,
                minute = 5,
                timeFormatPreference =
                    TimeFormatPreference
                        .TWELVE_HOUR
            )
        )

        assertEquals(
            "20:05",
            formatScheduleClock(
                hour = 20,
                minute = 5,
                timeFormatPreference =
                    TimeFormatPreference
                        .TWENTY_FOUR_HOUR
            )
        )
    }
}
