package com.iotkin.smartoutlet.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.iotkin.smartoutlet.data.model.DeviceAddress
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private const val DEVICE_SETTINGS_FILE =
    "device_settings"

val Context.deviceSettingsDataStore:
        DataStore<Preferences> by preferencesDataStore(
    name = DEVICE_SETTINGS_FILE
)

class DeviceSettingsStore(
    context: Context
) {
    private val dataStore =
        context.applicationContext
            .deviceSettingsDataStore

    private val safePreferences =
        dataStore.data.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }

    val savedDeviceAddress:
            Flow<DeviceAddress?> =
        safePreferences.map { preferences ->
            val host =
                preferences[Keys.DEVICE_HOST]
                    ?.trim()
                    .orEmpty()

            val port =
                preferences[Keys.DEVICE_PORT]
                    ?: DeviceAddress
                        .DEFAULT_API_PORT

            if (
                host.isBlank() ||
                port !in
                DeviceAddress.MIN_PORT..
                DeviceAddress.MAX_PORT
            ) {
                null
            } else {
                DeviceAddress(
                    host = host,
                    port = port
                )
            }
        }

    val appSettings:
            Flow<AppSettings> =
        safePreferences.map { preferences ->
            val friendlyName =
                preferences[
                    Keys.FRIENDLY_DEVICE_NAME
                ]
                    ?.trim()
                    ?.take(
                        AppSettings
                            .MAX_FRIENDLY_DEVICE_NAME_LENGTH
                    )
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?: AppSettings
                        .DEFAULT_FRIENDLY_DEVICE_NAME

            val pollingInterval =
                preferences[
                    Keys.POLLING_INTERVAL_SECONDS
                ]
                    ?.coerceIn(
                        AppSettings
                            .MIN_POLLING_INTERVAL_SECONDS,
                        AppSettings
                            .MAX_POLLING_INTERVAL_SECONDS
                    )
                    ?: AppSettings
                        .DEFAULT_POLLING_INTERVAL_SECONDS

            AppSettings(
                friendlyDeviceName =
                    friendlyName,
                automaticDiscoveryEnabled =
                    preferences[
                        Keys
                            .AUTOMATIC_DISCOVERY_ENABLED
                    ]
                        ?: true,
                pollingIntervalSeconds =
                    pollingInterval,
                themePreference =
                    preferences[
                        Keys.THEME_PREFERENCE
                    ].toAppThemePreference(),
                timeFormatPreference =
                    preferences[
                        Keys.TIME_FORMAT_PREFERENCE
                    ].toTimeFormatPreference()
            )
        }

    suspend fun saveDeviceAddress(
        address: DeviceAddress
    ) {
        dataStore.edit { preferences ->
            preferences[Keys.DEVICE_HOST] =
                address.host.trim()

            preferences[Keys.DEVICE_PORT] =
                address.port
        }
    }

    suspend fun clearDeviceAddress() {
        dataStore.edit { preferences ->
            preferences.remove(
                Keys.DEVICE_HOST
            )

            preferences.remove(
                Keys.DEVICE_PORT
            )
        }
    }

    suspend fun setFriendlyDeviceName(
        value: String
    ) {
        val sanitized =
            value
                .trim()
                .take(
                    AppSettings
                        .MAX_FRIENDLY_DEVICE_NAME_LENGTH
                )
                .ifBlank {
                    AppSettings
                        .DEFAULT_FRIENDLY_DEVICE_NAME
                }

        dataStore.edit { preferences ->
            preferences[
                Keys.FRIENDLY_DEVICE_NAME
            ] = sanitized
        }
    }

    suspend fun setAutomaticDiscoveryEnabled(
        enabled: Boolean
    ) {
        dataStore.edit { preferences ->
            preferences[
                Keys.AUTOMATIC_DISCOVERY_ENABLED
            ] = enabled
        }
    }

    suspend fun setPollingIntervalSeconds(
        seconds: Int
    ) {
        val safeValue =
            seconds.coerceIn(
                AppSettings
                    .MIN_POLLING_INTERVAL_SECONDS,
                AppSettings
                    .MAX_POLLING_INTERVAL_SECONDS
            )

        dataStore.edit { preferences ->
            preferences[
                Keys.POLLING_INTERVAL_SECONDS
            ] = safeValue
        }
    }

    suspend fun setThemePreference(
        preference: AppThemePreference
    ) {
        dataStore.edit { preferences ->
            preferences[
                Keys.THEME_PREFERENCE
            ] = preference.name
        }
    }

    suspend fun setTimeFormatPreference(
        preference: TimeFormatPreference
    ) {
        dataStore.edit { preferences ->
            preferences[
                Keys.TIME_FORMAT_PREFERENCE
            ] = preference.name
        }
    }

    suspend fun resetAppSettings() {
        dataStore.edit { preferences ->
            preferences.remove(
                Keys.FRIENDLY_DEVICE_NAME
            )

            preferences.remove(
                Keys.AUTOMATIC_DISCOVERY_ENABLED
            )

            preferences.remove(
                Keys.POLLING_INTERVAL_SECONDS
            )

            preferences.remove(
                Keys.THEME_PREFERENCE
            )

            preferences.remove(
                Keys.TIME_FORMAT_PREFERENCE
            )
        }
    }

    private fun String?
            .toAppThemePreference():
            AppThemePreference {

        return runCatching {
            AppThemePreference.valueOf(
                this.orEmpty()
            )
        }.getOrDefault(
            AppThemePreference.SYSTEM
        )
    }

    private fun String?
            .toTimeFormatPreference():
            TimeFormatPreference {

        return runCatching {
            TimeFormatPreference.valueOf(
                this.orEmpty()
            )
        }.getOrDefault(
            TimeFormatPreference.TWELVE_HOUR
        )
    }

    private object Keys {
        val DEVICE_HOST =
            stringPreferencesKey(
                name = "device_host"
            )

        val DEVICE_PORT =
            intPreferencesKey(
                name = "device_port"
            )

        val FRIENDLY_DEVICE_NAME =
            stringPreferencesKey(
                name =
                    "friendly_device_name"
            )

        val AUTOMATIC_DISCOVERY_ENABLED =
            booleanPreferencesKey(
                name =
                    "automatic_discovery_enabled"
            )

        val POLLING_INTERVAL_SECONDS =
            intPreferencesKey(
                name =
                    "polling_interval_seconds"
            )

        val THEME_PREFERENCE =
            stringPreferencesKey(
                name = "theme_preference"
            )

        val TIME_FORMAT_PREFERENCE =
            stringPreferencesKey(
                name =
                    "time_format_preference"
            )
    }
}