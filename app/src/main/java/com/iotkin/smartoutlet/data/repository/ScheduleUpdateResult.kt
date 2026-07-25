package com.iotkin.smartoutlet.data.repository

import com.iotkin.smartoutlet.data.model.RelayNumber
import com.iotkin.smartoutlet.data.model.RelayScheduleResponse

sealed interface ScheduleUpdateResult {

    data class Success(
        val relay: RelayNumber,
        val confirmedSchedule: RelayScheduleResponse
    ) : ScheduleUpdateResult

    data class ConfirmedButRefreshFailed(
        val relay: RelayNumber,
        val confirmedSchedule: RelayScheduleResponse,
        val message: String
    ) : ScheduleUpdateResult

    data class InvalidRequest(
        val message: String
    ) : ScheduleUpdateResult

    data object NoSavedDevice :
        ScheduleUpdateResult

    data object DeviceUnavailable :
        ScheduleUpdateResult

    data object SkippedAlreadyRunning :
        ScheduleUpdateResult

    data class Failed(
        val message: String
    ) : ScheduleUpdateResult
}