package com.iotkin.smartoutlet.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ScheduleUpdateRequest(
    val enabled: Boolean,
    val onHour: Int,
    val onMinute: Int,
    val offHour: Int,
    val offMinute: Int
) {
    fun validationError(): String? {
        return when {
            onHour !in 0..23 ||
                    offHour !in 0..23 -> {
                "Hours must be between 0 and 23."
            }

            onMinute !in 0..59 ||
                    offMinute !in 0..59 -> {
                "Minutes must be between 0 and 59."
            }

            onHour == offHour &&
                    onMinute == offMinute -> {
                "ON and OFF times must be different."
            }

            else -> null
        }
    }
}

@Serializable
data class ScheduleUpdateResponse(
    val success: Boolean,
    val relay: Int,
    val schedule: RelayScheduleResponse
)