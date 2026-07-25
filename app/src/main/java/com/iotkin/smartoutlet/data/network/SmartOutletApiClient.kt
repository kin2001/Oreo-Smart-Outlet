package com.iotkin.smartoutlet.data.network

import com.iotkin.smartoutlet.data.model.ApiErrorResponse
import com.iotkin.smartoutlet.data.model.RelayCommandRequest
import com.iotkin.smartoutlet.data.model.RelayNumber
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
            val response =
                apiService.getStatus()

            if (!response.isSuccessful) {
                DeviceStatusResult.HttpError(
                    statusCode = response.code(),
                    message =
                        readErrorMessage(response)
                )
            } else {
                val status = response.body()

                when {
                    status == null -> {
                        DeviceStatusResult
                            .InvalidResponse(
                                message =
                                    "The device returned an empty status response."
                            )
                    }

                    !status.success -> {
                        DeviceStatusResult
                            .InvalidResponse(
                                message =
                                    "The device reported that the status request failed."
                            )
                    }

                    status.device !=
                            EXPECTED_DEVICE_NAME -> {
                        DeviceStatusResult
                            .InvalidDevice(
                                receivedDevice =
                                    status.device
                            )
                    }

                    else -> {
                        DeviceStatusResult.Success(
                            status = status
                        )
                    }
                }
            }
        } catch (
            exception: SocketTimeoutException
        ) {
            DeviceStatusResult.Timeout
        } catch (
            exception: SerializationException
        ) {
            DeviceStatusResult.InvalidResponse(
                message =
                    "The device returned an invalid status response."
            )
        } catch (
            exception: IOException
        ) {
            DeviceStatusResult.NetworkError(
                message =
                    "Unable to reach the device."
            )
        }
    }

    suspend fun requestTimeSync():
            DeviceActionResult {

        return try {
            val response =
                apiService.requestTimeSync()

            if (!response.isSuccessful) {
                DeviceActionResult.HttpError(
                    statusCode = response.code(),
                    message =
                        readErrorMessage(response)
                )
            } else {
                val body = response.body()

                when {
                    body == null -> {
                        DeviceActionResult
                            .InvalidResponse(
                                message =
                                    "The device returned an empty time-sync response."
                            )
                    }

                    !body.success ||
                            !body.requested -> {
                        DeviceActionResult
                            .InvalidResponse(
                                message =
                                    "The device did not accept the time synchronization request."
                            )
                    }

                    else -> {
                        DeviceActionResult.Success
                    }
                }
            }
        } catch (
            exception: SocketTimeoutException
        ) {
            DeviceActionResult.Timeout
        } catch (
            exception: SerializationException
        ) {
            DeviceActionResult.InvalidResponse(
                message =
                    "The device returned an invalid time-sync response."
            )
        } catch (
            exception: IOException
        ) {
            DeviceActionResult.NetworkError(
                message =
                    "Unable to reach the device."
            )
        }
    }

    suspend fun setRelayState(
        relay: RelayNumber,
        desiredState: Boolean
    ): RelayApiResult {
        return try {
            val request =
                RelayCommandRequest
                    .fromDesiredState(
                        desiredState
                    )

            val response =
                when (relay) {
                    RelayNumber.RELAY_1 -> {
                        apiService.setRelay1(
                            request = request
                        )
                    }

                    RelayNumber.RELAY_2 -> {
                        apiService.setRelay2(
                            request = request
                        )
                    }
                }

            if (!response.isSuccessful) {
                RelayApiResult.HttpError(
                    statusCode = response.code(),
                    message =
                        readErrorMessage(response)
                )
            } else {
                val body = response.body()

                when {
                    body == null -> {
                        RelayApiResult.InvalidResponse(
                            message =
                                "The device returned an empty relay response."
                        )
                    }

                    !body.success -> {
                        RelayApiResult.InvalidResponse(
                            message =
                                "The device rejected the relay command."
                        )
                    }

                    body.relay != relay.apiValue -> {
                        RelayApiResult.InvalidResponse(
                            message =
                                "The device confirmed a different relay."
                        )
                    }

                    body.state != desiredState -> {
                        RelayApiResult.InvalidResponse(
                            message =
                                "${relay.displayName} did not confirm the requested state."
                        )
                    }

                    else -> {
                        RelayApiResult.Success(
                            relay = relay,
                            confirmedState =
                                body.state
                        )
                    }
                }
            }
        } catch (
            exception: SocketTimeoutException
        ) {
            RelayApiResult.Timeout
        } catch (
            exception: SerializationException
        ) {
            RelayApiResult.InvalidResponse(
                message =
                    "The device returned an invalid relay response."
            )
        } catch (
            exception: IOException
        ) {
            RelayApiResult.NetworkError(
                message =
                    "Unable to reach the device."
            )
        }
    }

    private fun readErrorMessage(
        response: Response<*>
    ): String {
        val responseText =
            response
                .errorBody()
                ?.string()
                ?.trim()

        if (responseText.isNullOrBlank()) {
            val statusMessage =
                response.message().trim()

            return if (
                statusMessage.isBlank()
            ) {
                "HTTP ${response.code()}"
            } else {
                "HTTP ${response.code()}: " +
                        statusMessage
            }
        }

        return runCatching {
            json.decodeFromString(
                deserializer =
                    ApiErrorResponse.serializer(),
                string = responseText
            ).error
        }.getOrElse {
            responseText.take(
                MAX_ERROR_MESSAGE_LENGTH
            )
        }
    }

    companion object {
        const val EXPECTED_DEVICE_NAME =
            "SmartOutlet_ESP8266"

        private const val
                MAX_ERROR_MESSAGE_LENGTH = 200
    }
}