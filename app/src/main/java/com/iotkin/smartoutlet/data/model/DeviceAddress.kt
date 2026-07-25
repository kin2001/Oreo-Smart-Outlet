package com.iotkin.smartoutlet.data.model

data class DeviceAddress(
    val host: String,
    val port: Int = DEFAULT_API_PORT
) {
    init {
        require(host.isNotBlank()) {
            "Device host must not be blank"
        }

        require(port in MIN_PORT..MAX_PORT) {
            "Device port must be between $MIN_PORT and $MAX_PORT"
        }
    }

    val displayAddress: String
        get() = "$host:$port"

    companion object {
        const val DEFAULT_API_PORT = 8080
        const val MIN_PORT = 1
        const val MAX_PORT = 65535
    }
}