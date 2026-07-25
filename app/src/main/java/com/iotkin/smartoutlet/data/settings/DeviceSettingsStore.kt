package com.iotkin.smartoutlet.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
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

private const val DEVICE_SETTINGS_FILE = "device_settings"

val Context.deviceSettingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = DEVICE_SETTINGS_FILE
)

class DeviceSettingsStore(
    context: Context
) {
    private val dataStore = context.applicationContext.deviceSettingsDataStore

    val savedDeviceAddress: Flow<DeviceAddress?> =
        dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val host = preferences[Keys.DEVICE_HOST]
                    ?.trim()
                    .orEmpty()

                val port = preferences[Keys.DEVICE_PORT]
                    ?: DeviceAddress.DEFAULT_API_PORT

                if (
                    host.isBlank() ||
                    port !in DeviceAddress.MIN_PORT..DeviceAddress.MAX_PORT
                ) {
                    null
                } else {
                    DeviceAddress(
                        host = host,
                        port = port
                    )
                }
            }

    suspend fun saveDeviceAddress(
        address: DeviceAddress
    ) {
        dataStore.edit { preferences ->
            preferences[Keys.DEVICE_HOST] = address.host.trim()
            preferences[Keys.DEVICE_PORT] = address.port
        }
    }

    suspend fun clearDeviceAddress() {
        dataStore.edit { preferences ->
            preferences.remove(Keys.DEVICE_HOST)
            preferences.remove(Keys.DEVICE_PORT)
        }
    }

    private object Keys {
        val DEVICE_HOST = stringPreferencesKey(
            name = "device_host"
        )

        val DEVICE_PORT = intPreferencesKey(
            name = "device_port"
        )
    }
}