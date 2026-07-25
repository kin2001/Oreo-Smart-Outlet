package com.iotkin.smartoutlet.data.model

import kotlinx.serialization.Serializable

@Serializable
data class RelayCommandRequest(
    val state: Boolean
) {
    companion object {
        val TURN_ON =
            RelayCommandRequest(
                state = true
            )

        val TURN_OFF =
            RelayCommandRequest(
                state = false
            )

        fun fromDesiredState(
            desiredState: Boolean
        ): RelayCommandRequest {
            return if (desiredState) {
                TURN_ON
            } else {
                TURN_OFF
            }
        }
    }
}

@Serializable
data class RelayCommandResponse(
    val success: Boolean,
    val relay: Int,
    val state: Boolean
)