package com.iotkin.smartoutlet.data.network

sealed interface DeviceActionResult {

    data object Success : DeviceActionResult

    data object Timeout : DeviceActionResult

    data object NoSavedDevice : DeviceActionResult

    data object SkippedAlreadyRunning : DeviceActionResult

    data class HttpError(
        val statusCode: Int,
        val message: String
    ) : DeviceActionResult

    data class InvalidResponse(
        val message: String
    ) : DeviceActionResult

    data class NetworkError(
        val message: String
    ) : DeviceActionResult
}