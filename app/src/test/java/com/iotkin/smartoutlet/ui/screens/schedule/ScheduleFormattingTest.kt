package com.iotkin.smartoutlet.ui.screens.schedule

import com.iotkin.smartoutlet.data.model.RelayScheduleResponse
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

    @Test
    fun nextScheduleActionUsesConfirmedDeviceTime() {
        val schedule =
            RelayScheduleResponse(
                enabled = true,
                onHour = 18,
                onMinute = 38,
                offHour = 4,
                offMinute = 0
            )

        assertEquals(
            "Next action: Turns ON in 6 hr 2 min",
            formatNextScheduleAction(
                schedule = schedule,
                confirmedPhilippineTime =
                    "2026-07-28T12:35:44+08:00"
            )
        )

        assertEquals(
            "Next action: Turns OFF in 9 hr",
            formatNextScheduleAction(
                schedule = schedule,
                confirmedPhilippineTime =
                    "2026-07-28T19:00:00+08:00"
            )
        )

        assertEquals(
            "Next action: Schedule disabled",
            formatNextScheduleAction(
                schedule = schedule.copy(
                    enabled = false
                ),
                confirmedPhilippineTime =
                    "2026-07-28T12:35:44+08:00"
            )
        )

        assertEquals(
            "Next action: Waiting for live device time",
            formatNextScheduleAction(
                schedule = schedule,
                confirmedPhilippineTime = null
            )
        )
    }
}
