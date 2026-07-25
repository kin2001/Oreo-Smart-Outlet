package com.iotkin.smartoutlet.data.network

import com.iotkin.smartoutlet.data.model.RelayNumber

sealed interface RelayApiResult {

    data class Success(
        val relay: RelayNumber,
        val confirmedState: Boolean
    ) : RelayApiResult

    data object Timeout : RelayApiResult

    data class HttpError(
        val statusCode: Int,
        val message: String
    ) : RelayApiResult

    data class InvalidResponse(
        val message: String
    ) : RelayApiResult

    data class NetworkError(
        val message: String
    ) : RelayApiResult
}