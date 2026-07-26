package com.iotkin.smartoutlet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
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
            val darkTheme =
                settings.themePreference
                    .isDarkTheme(
                        isSystemInDarkTheme()
                    )

            OreoSmartOutletTheme(
                darkTheme = darkTheme
            ) {
                val systemBarColor =
                    MaterialTheme.colorScheme
                        .background
                        .toArgb()

                SideEffect {
                    window.statusBarColor =
                        systemBarColor
                    window.navigationBarColor =
                        systemBarColor

                    WindowCompat
                        .getInsetsController(
                            window,
                            window.decorView
                        )
                        .apply {
                            isAppearanceLightStatusBars =
                                !darkTheme
                            isAppearanceLightNavigationBars =
                                !darkTheme
                        }
                }

                AppNavHost()
            }
        }
    }
}
