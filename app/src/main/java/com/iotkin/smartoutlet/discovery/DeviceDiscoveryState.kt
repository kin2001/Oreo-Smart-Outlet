package com.iotkin.smartoutlet.discovery

import com.iotkin.smartoutlet.data.model.DeviceAddress

data class DiscoveredSmartOutlet(
    val serviceName: String,
    val address: DeviceAddress,
    val deviceId: String,
    val firmwareVersion: String,
    val rssi: Int
) {
    val stableKey: String
        get() = "${address.host.lowercase()}:${address.port}"
}

data class DeviceDiscoveryUiState(
    val isDiscovering: Boolean = false,
    val isValidating: Boolean = false,
    val devices: List<DiscoveredSmartOutlet> = emptyList(),
    val errorMessage: String? = null,
    val hasCompletedSearchWindow: Boolean = false
) {
    val showNoDevicesFound: Boolean
        get() = hasCompletedSearchWindow &&
                devices.isEmpty() &&
                errorMessage == null
}