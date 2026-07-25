package com.iotkin.smartoutlet.ui.screens.schedule

import com.iotkin.smartoutlet.data.model.RelayNumber
import com.iotkin.smartoutlet.data.model.RelayScheduleResponse

enum class ScheduleMeridiem {
    AM,
    PM
}

data class ScheduleTimeInput(
    val hourText: String = "12",
    val minuteText: String = "00",
    val meridiem: ScheduleMeridiem =
        ScheduleMeridiem.AM
) {
    fun validationError(
        label: String
    ): String? {
        val hour =
            hourText.toIntOrNull()

        val minute =
            minuteText.toIntOrNull()

        return when {
            hour == null -> {
                "$label hour is required."
            }

            hour !in 1..12 -> {
                "$label hour must be between 1 and 12."
            }

            minute == null -> {
                "$label minute is required."
            }

            minute !in 0..59 -> {
                "$label minute must be between 0 and 59."
            }

            else -> null
        }
    }

    fun to24Hour(): Int? {
        val hour =
            hourText.toIntOrNull()
                ?: return null

        if (hour !in 1..12) {
            return null
        }

        return when (meridiem) {
            ScheduleMeridiem.AM -> {
                if (hour == 12) {
                    0
                } else {
                    hour
                }
            }

            ScheduleMeridiem.PM -> {
                if (hour == 12) {
                    12
                } else {
                    hour + 12
                }
            }
        }
    }

    fun minuteValue(): Int? {
        val minute =
            minuteText.toIntOrNull()
                ?: return null

        return minute.takeIf {
            it in 0..59
        }
    }

    companion object {
        fun from24Hour(
            hour: Int,
            minute: Int
        ): ScheduleTimeInput {
            val safeHour =
                hour.coerceIn(0, 23)

            val safeMinute =
                minute.coerceIn(0, 59)

            val displayHour =
                when (val value = safeHour % 12) {
                    0 -> 12
                    else -> value
                }

            val meridiem =
                if (safeHour < 12) {
                    ScheduleMeridiem.AM
                } else {
                    ScheduleMeridiem.PM
                }

            return ScheduleTimeInput(
                hourText =
                    displayHour.toString(),
                minuteText =
                    safeMinute
                        .toString()
                        .padStart(
                            length = 2,
                            padChar = '0'
                        ),
                meridiem = meridiem
            )
        }
    }
}

data class ScheduleEditorUiState(
    val relay: RelayNumber? = null,
    val enabled: Boolean = false,
    val onTime: ScheduleTimeInput =
        ScheduleTimeInput(),
    val offTime: ScheduleTimeInput =
        ScheduleTimeInput(),
    val isSaving: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val validationError: String? = null,
    val message: String? = null,
    val error: String? = null
) {
    val isLoaded: Boolean
        get() = relay != null

    companion object {
        fun fromSchedule(
            relay: RelayNumber,
            schedule: RelayScheduleResponse,
            message: String? = null,
            error: String? = null
        ): ScheduleEditorUiState {
            return ScheduleEditorUiState(
                relay = relay,
                enabled = schedule.enabled,
                onTime =
                    ScheduleTimeInput.from24Hour(
                        hour = schedule.onHour,
                        minute = schedule.onMinute
                    ),
                offTime =
                    ScheduleTimeInput.from24Hour(
                        hour = schedule.offHour,
                        minute = schedule.offMinute
                    ),
                isSaving = false,
                hasUnsavedChanges = false,
                validationError = null,
                message = message,
                error = error
            )
        }
    }
}