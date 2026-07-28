package com.iotkin.smartoutlet.ui.screens.connection

import com.iotkin.smartoutlet.data.model.DeviceAddress
import com.iotkin.smartoutlet.data.settings.AppSettings
import com.iotkin.smartoutlet.discovery.DeviceDiscoveryUiState

enum class ConnectionIssueType {
    TIMEOUT,
    INVALID_DEVICE,
    CONNECTION_FAILED
}

enum class SavedDeviceConnectionStatus {
    SAVED,
    ONLINE,
    RECONNECTING,
    STALE,
    OFFLINE
}

data class DeviceConnectionSetupUiState(
    val savedAddress: DeviceAddress? = null,
    val friendlyDeviceName: String =
        AppSettings.DEFAULT_FRIENDLY_DEVICE_NAME,
    val automaticDiscoveryEnabled: Boolean = true,
    val ipAddress: String = "",
    val port: String =
        DeviceAddress.DEFAULT_API_PORT.toString(),
    val ipError: String? = null,
    val portError: String? = null,
    val isTestingConnection: Boolean = false,
    val isSavingDevice: Boolean = false,
    val isDeviceVerified: Boolean = false,
    val hasVerifiedLiveResponse: Boolean = false,
    val isDisconnectingDevice: Boolean = false,
    val verifiedAddress: DeviceAddress? = null,
    val connectionIssue: ConnectionIssueType? = null,
    val connectionMessage: String? = null,
    val saveError: String? = null,
    val discovery: DeviceDiscoveryUiState =
        DeviceDiscoveryUiState()
) {
    val savedDeviceConnectionStatus:
            SavedDeviceConnectionStatus?
        get() {
            if (savedAddress == null) {
                return null
            }

            return when {
                isTestingConnection ->
                    SavedDeviceConnectionStatus
                        .RECONNECTING

                isDeviceVerified ->
                    SavedDeviceConnectionStatus.ONLINE

                connectionIssue ==
                        ConnectionIssueType.INVALID_DEVICE ->
                    SavedDeviceConnectionStatus.OFFLINE

                hasVerifiedLiveResponse &&
                        connectionIssue != null ->
                    SavedDeviceConnectionStatus.STALE

                connectionIssue != null ->
                    SavedDeviceConnectionStatus.OFFLINE

                else ->
                    SavedDeviceConnectionStatus.SAVED
            }
        }
}
