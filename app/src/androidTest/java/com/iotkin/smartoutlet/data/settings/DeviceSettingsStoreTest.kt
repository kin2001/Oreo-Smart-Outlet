package com.iotkin.smartoutlet.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.iotkin.smartoutlet.data.model.RelayNumber
import java.io.File
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeviceSettingsStoreTest {

    private lateinit var dataStoreScope:
            CoroutineScope

    private lateinit var settingsStore:
            DeviceSettingsStore

    @Before
    fun setUp() {
        val context =
            InstrumentationRegistry
                .getInstrumentation()
                .targetContext

        dataStoreScope =
            CoroutineScope(
                SupervisorJob() +
                        Dispatchers.IO
            )

        val dataStore =
            isolatedDataStore(
                context = context,
                scope = dataStoreScope
            )

        settingsStore =
            DeviceSettingsStore(
                context = context,
                dataStore = dataStore
            )
    }

    @After
    fun tearDown() {
        dataStoreScope.cancel()
    }

    @Test
    fun outletNamesPersistTrimmedAndLimited() =
        runBlocking {
            settingsStore
                .setOutletFriendlyName(
                    relay = RelayNumber.RELAY_1,
                    value = "  Coffee Maker  "
                )

            settingsStore
                .setOutletFriendlyName(
                    relay = RelayNumber.RELAY_2,
                    value = "x".repeat(50)
                )

            val restored =
                settingsStore.appSettings
                    .first()

            assertEquals(
                "Coffee Maker",
                restored.outlet1FriendlyName
            )

            assertEquals(
                40,
                restored
                    .outlet2FriendlyName
                    .length
            )
        }

    @Test
    fun resetRestoresDefaultOutletNames() =
        runBlocking {
            settingsStore
                .setOutletFriendlyName(
                    relay = RelayNumber.RELAY_1,
                    value = "Desk Lamp"
                )

            settingsStore
                .setOutletFriendlyName(
                    relay = RelayNumber.RELAY_2,
                    value = "Fan"
                )

            settingsStore.resetAppSettings()

            val resetSettings =
                settingsStore.appSettings
                    .first()

            assertEquals(
                AppSettings
                    .DEFAULT_OUTLET_1_FRIENDLY_NAME,
                resetSettings
                    .outlet1FriendlyName
            )

            assertEquals(
                AppSettings
                    .DEFAULT_OUTLET_2_FRIENDLY_NAME,
                resetSettings
                    .outlet2FriendlyName
            )
        }

    private fun isolatedDataStore(
        context: Context,
        scope: CoroutineScope
    ): DataStore<Preferences> {
        return PreferenceDataStoreFactory
            .create(
                scope = scope,
                produceFile = {
                    File(
                        context.cacheDir,
                        "device-settings-" +
                                UUID.randomUUID() +
                                ".preferences_pb"
                    )
                }
            )
    }
}
