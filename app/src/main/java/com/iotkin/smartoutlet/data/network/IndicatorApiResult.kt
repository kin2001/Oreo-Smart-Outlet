package com.iotkin.smartoutlet.data.network

sealed interface IndicatorApiResult {

    data class Success(
        val confirmedEnabled: Boolean
    ) : IndicatorApiResult

    data object Timeout : IndicatorApiResult

    data class HttpError(
        val statusCode: Int,
        val message: String
    ) : IndicatorApiResult

    data class InvalidResponse(
        val message: String
    ) : IndicatorApiResult

    data class NetworkError(
        val message: String
    ) : IndicatorApiResult
}
