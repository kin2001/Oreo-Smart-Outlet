package com.iotkin.smartoutlet.data.settings

import com.iotkin.smartoutlet.data.model.RelayNumber

enum class AppThemePreference {
    SYSTEM,
    LIGHT,
    DARK
}

internal fun AppThemePreference.isDarkTheme(
    systemInDarkTheme: Boolean
): Boolean {
    return when (this) {
        AppThemePreference.SYSTEM ->
            systemInDarkTheme

        AppThemePreference.LIGHT ->
            false

        AppThemePreference.DARK ->
            true
    }
}

enum class TimeFormatPreference {
    TWELVE_HOUR,
    TWENTY_FOUR_HOUR
}

data class AppSettings(
    val friendlyDeviceName: String =
        DEFAULT_FRIENDLY_DEVICE_NAME,
    val outlet1FriendlyName: String =
        DEFAULT_OUTLET_1_FRIENDLY_NAME,
    val outlet2FriendlyName: String =
        DEFAULT_OUTLET_2_FRIENDLY_NAME,
    val automaticDiscoveryEnabled: Boolean = true,
    val pollingIntervalSeconds: Int =
        DEFAULT_POLLING_INTERVAL_SECONDS,
    val themePreference: AppThemePreference =
        AppThemePreference.SYSTEM,
    val timeFormatPreference: TimeFormatPreference =
        TimeFormatPreference.TWELVE_HOUR
) {
    fun outletFriendlyName(
        relay: RelayNumber
    ): String {
        return when (relay) {
            RelayNumber.RELAY_1 ->
                outlet1FriendlyName

            RelayNumber.RELAY_2 ->
                outlet2FriendlyName
        }
    }

    companion object {
        const val DEFAULT_FRIENDLY_DEVICE_NAME =
            "Oreo Smart Outlet"

        const val DEFAULT_OUTLET_1_FRIENDLY_NAME =
            "Outlet 1"

        const val DEFAULT_OUTLET_2_FRIENDLY_NAME =
            "Outlet 2"

        const val MIN_POLLING_INTERVAL_SECONDS = 2
        const val MAX_POLLING_INTERVAL_SECONDS = 30
        const val DEFAULT_POLLING_INTERVAL_SECONDS = 2

        const val MAX_FRIENDLY_DEVICE_NAME_LENGTH = 40
    }
}
