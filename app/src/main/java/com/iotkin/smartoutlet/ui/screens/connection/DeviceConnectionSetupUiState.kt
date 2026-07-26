package com.iotkin.smartoutlet.ui.screens.connection

import com.iotkin.smartoutlet.data.model.DeviceAddress
import com.iotkin.smartoutlet.discovery.DeviceDiscoveryUiState

enum class ConnectionIssueType {
    TIMEOUT,
    INVALID_DEVICE,
    CONNECTION_FAILED
}

data class DeviceConnectionSetupUiState(
    val savedAddress: DeviceAddress? = null,
    val automaticDiscoveryEnabled: Boolean = true,
    val ipAddress: String = "",
    val port: String =
        DeviceAddress.DEFAULT_API_PORT.toString(),
    val ipError: String? = null,
    val portError: String? = null,
    val isTestingConnection: Boolean = false,
    val isSavingDevice: Boolean = false,
    val isDeviceVerified: Boolean = false,
    val isDisconnectingDevice: Boolean = false,
    val verifiedAddress: DeviceAddress? = null,
    val connectionIssue: ConnectionIssueType? = null,
    val connectionMessage: String? = null,
    val saveError: String? = null,
    val discovery: DeviceDiscoveryUiState =
        DeviceDiscoveryUiState()
)