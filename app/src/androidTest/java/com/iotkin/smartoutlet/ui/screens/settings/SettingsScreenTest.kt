package com.iotkin.smartoutlet.ui.screens.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.iotkin.smartoutlet.ui.theme.OreoSmartOutletTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun statusRefreshUsesSingleChoiceDialog() {
        var selectedInterval: Int? = null

        composeTestRule.setContent {
            OreoSmartOutletTheme(
                darkTheme = false
            ) {
                SettingsScreen(
                    state = SettingsUiState(),
                    onSaveFriendlyName = {},
                    onAutomaticDiscoveryChange = {},
                    onPollingIntervalChange = {
                        selectedInterval = it
                    },
                    onThemeChange = {},
                    onTimeFormatChange = {},
                    onManageDevice = {},
                    onResetSettings = {},
                    onAbout = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Status Refresh")
            .performScrollTo()
            .performClick()

        composeTestRule
            .onNodeWithText(
                "Every 30 seconds"
            )
            .assertIsDisplayed()
            .performClick()

        composeTestRule.runOnIdle {
            assertEquals(
                30,
                selectedInterval
            )
        }
    }
}
