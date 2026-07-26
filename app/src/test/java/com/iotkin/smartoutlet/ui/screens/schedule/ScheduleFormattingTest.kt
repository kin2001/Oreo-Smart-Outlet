package com.iotkin.smartoutlet.ui.screens.schedule

import com.iotkin.smartoutlet.data.settings.TimeFormatPreference
import org.junit.Assert.assertEquals
import org.junit.Test

class ScheduleFormattingTest {

    @Test
    fun scheduleTimeUsesSavedTimeFormat() {
        assertEquals(
            "8:05 PM",
            formatScheduleTime(
                hour = 20,
                minute = 5,
                timeFormatPreference =
                    TimeFormatPreference
                        .TWELVE_HOUR
            )
        )

        assertEquals(
            "20:05",
            formatScheduleTime(
                hour = 20,
                minute = 5,
                timeFormatPreference =
                    TimeFormatPreference
                        .TWENTY_FOUR_HOUR
            )
        )
    }
}
