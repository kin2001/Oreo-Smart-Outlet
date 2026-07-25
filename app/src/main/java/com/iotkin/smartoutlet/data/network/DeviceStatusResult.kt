package com.iotkin.smartoutlet.data.network

import com.iotkin.smartoutlet.data.model.DeviceStatusResponse

sealed class DeviceStatusResult {

    data class Success(
        val status: DeviceStatusResponse
    ) : DeviceStatusResult()

    data class HttpError(
        val statusCode: Int,
        val message: String
    ) : DeviceStatusResult()

    data class InvalidDevice(
        val receivedDevice: String
    ) : DeviceStatusResult()

    data class InvalidResponse(
        val message: String
    ) : DeviceStatusResult()

    data class NetworkError(
        val message: String
    ) : DeviceStatusResult()

    data object Timeout : DeviceStatusResult()
}