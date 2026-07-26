package com.iotkin.smartoutlet

import com.iotkin.smartoutlet.data.settings.AppThemePreference
import com.iotkin.smartoutlet.data.settings.isDarkTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppThemePreferenceTest {

    @Test
    fun themePreferenceResolvesSystemAndOverrides() {
        assertTrue(
            AppThemePreference.SYSTEM
                .isDarkTheme(
                    systemInDarkTheme = true
                )
        )

        assertFalse(
            AppThemePreference.SYSTEM
                .isDarkTheme(
                    systemInDarkTheme = false
                )
        )

        assertFalse(
            AppThemePreference.LIGHT
                .isDarkTheme(
                    systemInDarkTheme = true
                )
        )

        assertTrue(
            AppThemePreference.DARK
                .isDarkTheme(
                    systemInDarkTheme = false
                )
        )
    }
}
