package com.iotkin.smartoutlet.discovery

import com.iotkin.smartoutlet.data.model.DeviceAddress

data class DiscoveredServiceCandidate(
    val serviceName: String,
    val host: String,
    val port: Int
) {
    val address: DeviceAddress
        get() = DeviceAddress(
            host = host,
            port = port
        )

    val stableKey: String
        get() = "${host.lowercase()}:$port"
}

data class NsdDiscoveryState(
    val isDiscovering: Boolean = false,
    val candidates: List<DiscoveredServiceCandidate> = emptyList(),
    val errorMessage: String? = null
)