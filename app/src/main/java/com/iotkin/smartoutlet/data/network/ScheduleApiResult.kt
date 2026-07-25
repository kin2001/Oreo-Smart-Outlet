package com.iotkin.smartoutlet.data.network

import com.iotkin.smartoutlet.data.model.RelayNumber
import com.iotkin.smartoutlet.data.model.RelayScheduleResponse

sealed interface ScheduleApiResult {

    data class Success(
        val relay: RelayNumber,
        val confirmedSchedule: RelayScheduleResponse
    ) : ScheduleApiResult

    data class InvalidRequest(
        val message: String
    ) : ScheduleApiResult

    data object Timeout : ScheduleApiResult

    data class HttpError(
        val statusCode: Int,
        val message: String
    ) : ScheduleApiResult

    data class InvalidResponse(
        val message: String
    ) : ScheduleApiResult

    data class NetworkError(
        val message: String
    ) : ScheduleApiResult
}