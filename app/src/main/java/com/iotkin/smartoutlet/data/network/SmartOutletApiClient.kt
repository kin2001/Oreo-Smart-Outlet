package com.iotkin.smartoutlet.data.network

import com.iotkin.smartoutlet.data.model.ApiErrorResponse
import java.io.IOException
import java.net.SocketTimeoutException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.Response

class SmartOutletApiClient(
    private val apiService: SmartOutletApiService,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }
) {

    suspend fun getStatus(): DeviceStatusResult {
        return try {
            val response = apiService.getStatus()

            if (!response.isSuccessful) {
                DeviceStatusResult.HttpError(
                    statusCode = response.code(),
                    message = readErrorMessage(response)
                )
            } else {
                val status = response.body()

                when {
                    status == null -> {
                        DeviceStatusResult.InvalidResponse(
                            message = "The device returned an empty status response."
                        )
                    }

                    !status.success -> {
                        DeviceStatusResult.InvalidResponse(
                            message = "The device reported that the status request failed."
                        )
                    }

                    status.device != EXPECTED_DEVICE_NAME -> {
                        DeviceStatusResult.InvalidDevice(
                            receivedDevice = status.device
                        )
                    }

                    else -> {
                        DeviceStatusResult.Success(
                            status = status
                        )
                    }
                }
            }
        } catch (exception: SocketTimeoutException) {
            DeviceStatusResult.Timeout
        } catch (exception: SerializationException) {
            DeviceStatusResult.InvalidResponse(
                message = "The device returned an invalid status response."
            )
        } catch (exception: IOException) {
            DeviceStatusResult.NetworkError(
                message = "Unable to reach the device."
            )
        }
    }

    private fun readErrorMessage(
        response: Response<*>
    ): String {
        val responseText = response
            .errorBody()
            ?.string()
            ?.trim()

        if (responseText.isNullOrBlank()) {
            val statusMessage = response.message().trim()

            return if (statusMessage.isBlank()) {
                "HTTP ${response.code()}"
            } else {
                "HTTP ${response.code()}: $statusMessage"
            }
        }

        return runCatching {
            json.decodeFromString(
                deserializer = ApiErrorResponse.serializer(),
                string = responseText
            ).error
        }.getOrElse {
            responseText.take(MAX_ERROR_MESSAGE_LENGTH)
        }
    }

    companion object {
        const val EXPECTED_DEVICE_NAME =
            "SmartOutlet_ESP8266"

        private const val MAX_ERROR_MESSAGE_LENGTH =
            200
    }
}