package com.iotkin.smartoutlet.data.model

enum class RelayNumber(
    val apiValue: Int,
    val displayName: String
) {
    RELAY_1(
        apiValue = 1,
        displayName = "Relay 1"
    ),

    RELAY_2(
        apiValue = 2,
        displayName = "Relay 2"
    )
}