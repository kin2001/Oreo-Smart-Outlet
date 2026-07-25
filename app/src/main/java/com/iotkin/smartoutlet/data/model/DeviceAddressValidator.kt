package com.iotkin.smartoutlet.data.model

data class DeviceAddressValidationResult(
    val address: DeviceAddress? = null,
    val ipError: String? = null,
    val portError: String? = null
) {
    val isValid: Boolean
        get() = address != null &&
                ipError == null &&
                portError == null
}

object DeviceAddressValidator {

    fun validate(
        ipInput: String,
        portInput: String
    ): DeviceAddressValidationResult {
        val ipAddress = ipInput.trim()
        val portText = portInput.trim()

        val ipError = validateIpAddress(ipAddress)
        val portError = validatePort(portText)

        if (ipError != null || portError != null) {
            return DeviceAddressValidationResult(
                ipError = ipError,
                portError = portError
            )
        }

        return DeviceAddressValidationResult(
            address = DeviceAddress(
                host = ipAddress,
                port = portText.toInt()
            )
        )
    }

    fun validateIpAddress(input: String): String? {
        val value = input.trim()

        if (value.isBlank()) {
            return "Enter the device IP address"
        }

        val parts = value.split(".")

        if (parts.size != 4) {
            return "Enter a valid IPv4 address"
        }

        for (part in parts) {
            if (
                part.isEmpty() ||
                part.any { character -> !character.isDigit() }
            ) {
                return "Enter a valid IPv4 address"
            }

            if (
                part.length > 1 &&
                part.startsWith("0")
            ) {
                return "Do not use leading zeros in the IP address"
            }

            val number = part.toIntOrNull()

            if (number == null || number !in 0..255) {
                return "Each IP section must be between 0 and 255"
            }
        }

        if (
            value == "0.0.0.0" ||
            value == "255.255.255.255"
        ) {
            return "Enter the smart outlet's actual IP address"
        }

        return null
    }

    fun validatePort(input: String): String? {
        val value = input.trim()

        if (value.isBlank()) {
            return "Enter the API port"
        }

        if (value.any { character -> !character.isDigit() }) {
            return "The port must contain numbers only"
        }

        val port = value.toIntOrNull()
            ?: return "Enter a valid port"

        if (
            port < DeviceAddress.MIN_PORT ||
            port > DeviceAddress.MAX_PORT
        ) {
            return "The port must be between 1 and 65535"
        }

        return null
    }
}