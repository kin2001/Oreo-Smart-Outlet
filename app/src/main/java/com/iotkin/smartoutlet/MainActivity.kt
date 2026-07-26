package com.iotkin.smartoutlet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iotkin.smartoutlet.data.settings.AppSettings
import com.iotkin.smartoutlet.data.settings.DeviceSettingsStore
import com.iotkin.smartoutlet.data.settings.isDarkTheme
import com.iotkin.smartoutlet.ui.navigation.AppNavHost
import com.iotkin.smartoutlet.ui.theme.OreoSmartOutletTheme

class MainActivity : ComponentActivity() {

    private val settingsStore by lazy {
        DeviceSettingsStore(
            applicationContext
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val settings by
                settingsStore.appSettings
                    .collectAsStateWithLifecycle(
                        initialValue =
                            AppSettings()
                    )

            OreoSmartOutletTheme(
                darkTheme =
                    settings.themePreference
                        .isDarkTheme(
                            isSystemInDarkTheme()
                        )
            ) {
                AppNavHost()
            }
        }
    }
}
