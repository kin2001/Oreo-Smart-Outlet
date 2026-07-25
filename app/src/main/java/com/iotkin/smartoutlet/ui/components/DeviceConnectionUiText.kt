package com.iotkin.smartoutlet.ui.components

import com.iotkin.smartoutlet.data.repository.DeviceConnectionState
import com.iotkin.smartoutlet.data.repository.DeviceStatusRepositoryState

fun DeviceStatusRepositoryState.connectionLabel():
        String {

    return when {
        isStale &&
                connectionState ==
                DeviceConnectionState.RECONNECTING -> {
            "Reconnecting"
        }

        isStale &&
                connectionState ==
                DeviceConnectionState.OFFLINE -> {
            "Offline"
        }

        connectionState ==
                DeviceConnectionState.NO_SAVED_DEVICE -> {
            "No device"
        }

        connectionState ==
                DeviceConnectionState.CONNECTING -> {
            "Connecting"
        }

        connectionState ==
                DeviceConnectionState.ONLINE -> {
            "Online"
        }

        connectionState ==
                DeviceConnectionState.RECONNECTING -> {
            "Reconnecting"
        }

        else -> {
            "Offline"
        }
    }
}

fun DeviceStatusRepositoryState.connectionDescription():
        String {

    return when {
        connectionState ==
                DeviceConnectionState.NO_SAVED_DEVICE -> {
            "No smart outlet address is saved."
        }

        connectionState ==
                DeviceConnectionState.CONNECTING -> {
            "Connecting to the saved smart outlet."
        }

        connectionState ==
                DeviceConnectionState.ONLINE &&
                !isStale -> {
            "Live device data is available."
        }

        connectionState ==
                DeviceConnectionState.RECONNECTING &&
                hasConfirmedStatus -> {
            "Showing the last confirmed data while reconnecting."
        }

        connectionState ==
                DeviceConnectionState.RECONNECTING -> {
            "Trying to reconnect to the smart outlet."
        }

        connectionState ==
                DeviceConnectionState.OFFLINE &&
                hasConfirmedStatus -> {
            "Showing the last confirmed data. Live controls are unavailable."
        }

        else -> {
            "The smart outlet is offline."
        }
    }
}

fun DeviceStatusRepositoryState.writeUnavailableMessage(
    featureName: String
): String? {
    if (
        connectionState ==
        DeviceConnectionState.ONLINE &&
        !isStale
    ) {
        return null
    }

    return when (connectionState) {
        DeviceConnectionState.NO_SAVED_DEVICE -> {
            "$featureName unavailable because no smart outlet is saved."
        }

        DeviceConnectionState.CONNECTING -> {
            "$featureName unavailable while connecting."
        }

        DeviceConnectionState.RECONNECTING -> {
            "$featureName unavailable while reconnecting. Displayed data may be stale."
        }

        DeviceConnectionState.OFFLINE -> {
            "$featureName unavailable while the smart outlet is offline. Displayed data is the last confirmed state."
        }

        DeviceConnectionState.ONLINE -> {
            "$featureName unavailable because the displayed data is stale."
        }
    }
}