package com.iotkin.smartoutlet.data.network

import com.iotkin.smartoutlet.data.model.ApiErrorResponse
import com.iotkin.smartoutlet.data.model.IndicatorSettingsRequest
import com.iotkin.smartoutlet.data.model.RelayCommandRequest
import com.iotkin.smartoutlet.data.model.RelayNumber
import java.io.IOException
import java.net.SocketTimeoutException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.Response
import com.iotkin.smartoutlet.data.model.RelayScheduleResponse
import com.iotkin.smartoutlet.data.model.ScheduleUpdateRequest

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

    suspend fun setIndicatorsEnabled(
        enabled: Boolean
    ): IndicatorApiResult {
        return try {
            val response =
                apiService.setIndicatorsEnabled(
                    request =
                        IndicatorSettingsRequest(
                            enabled = enabled
                        )
                )

            if (!response.isSuccessful) {
                IndicatorApiResult.HttpError(
                    statusCode = response.code(),
                    message =
                        readErrorMessage(response)
                )
            } else {
                val body = response.body()

                when {
                    body == null -> {
                        IndicatorApiResult.InvalidResponse(
                            message =
                                "The device returned an empty indicator response."
                        )
                    }

                    !body.success -> {
                        IndicatorApiResult.InvalidResponse(
                            message =
                                "The device rejected the indicator setting."
                        )
                    }

                    body.indicatorsEnabled != enabled -> {
                        IndicatorApiResult.InvalidResponse(
                            message =
                                "The device confirmed a different indicator setting."
                        )
                    }

                    else -> {
                        IndicatorApiResult.Success(
                            confirmedEnabled =
                                body.indicatorsEnabled
                        )
                    }
                }
            }
        } catch (
            exception: SocketTimeoutException
        ) {
            IndicatorApiResult.Timeout
        } catch (
            exception: SerializationException
        ) {
            IndicatorApiResult.InvalidResponse(
                message =
                    "The device returned an invalid indicator response."
            )
        } catch (
            exception: IOException
        ) {
            IndicatorApiResult.NetworkError(
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

    private fun scheduleMatches(
        actual: RelayScheduleResponse,
        requested: ScheduleUpdateRequest
    ): Boolean {
        return actual.enabled ==
                requested.enabled &&
                actual.onHour ==
                requested.onHour &&
                actual.onMinute ==
                requested.onMinute &&
                actual.offHour ==
                requested.offHour &&
                actual.offMinute ==
                requested.offMinute
    }

    suspend fun updateSchedule(
        relay: RelayNumber,
        request: ScheduleUpdateRequest
    ): ScheduleApiResult {
        val validationError =
            request.validationError()

        if (validationError != null) {
            return ScheduleApiResult.InvalidRequest(
                message = validationError
            )
        }

        return try {
            val response =
                when (relay) {
                    RelayNumber.RELAY_1 -> {
                        apiService.updateSchedule1(
                            request = request
                        )
                    }

                    RelayNumber.RELAY_2 -> {
                        apiService.updateSchedule2(
                            request = request
                        )
                    }
                }

            if (!response.isSuccessful) {
                ScheduleApiResult.HttpError(
                    statusCode = response.code(),
                    message = readErrorMessage(
                        response
                    )
                )
            } else {
                val body = response.body()

                when {
                    body == null -> {
                        ScheduleApiResult.InvalidResponse(
                            message =
                                "The device returned an empty schedule response."
                        )
                    }

                    !body.success -> {
                        ScheduleApiResult.InvalidResponse(
                            message =
                                "The device did not confirm the schedule update."
                        )
                    }

                    body.relay != relay.apiValue -> {
                        ScheduleApiResult.InvalidResponse(
                            message =
                                "The device confirmed a different outlet schedule."
                        )
                    }

                    !scheduleMatches(
                        actual = body.schedule,
                        requested = request
                    ) -> {
                        ScheduleApiResult.InvalidResponse(
                            message =
                                "The schedule returned by the device does not match the requested values."
                        )
                    }

                    else -> {
                        ScheduleApiResult.Success(
                            relay = relay,
                            confirmedSchedule =
                                body.schedule
                        )
                    }
                }
            }
        } catch (
            exception: SocketTimeoutException
        ) {
            ScheduleApiResult.Timeout
        } catch (
            exception: SerializationException
        ) {
            ScheduleApiResult.InvalidResponse(
                message =
                    "The device returned an invalid schedule response."
            )
        } catch (
            exception: IOException
        ) {
            ScheduleApiResult.NetworkError(
                message =
                    "Unable to reach the device."
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
