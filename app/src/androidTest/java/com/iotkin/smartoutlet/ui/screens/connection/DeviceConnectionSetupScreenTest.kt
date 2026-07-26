package com.iotkin.smartoutlet.ui.screens.connection

import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.iotkin.smartoutlet.ui.theme.OreoSmartOutletTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeviceConnectionSetupScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun backIsHiddenWhenConnectionIsTheStartupScreen() {
        showScreen()

        composeTestRule
            .onNodeWithText("Back")
            .assertDoesNotExist()
    }

    @Test
    fun backIsShownAndClickableWhenOpenedFromSettings() {
        var backClicked = false

        showScreen(
            onBack = {
                backClicked = true
            }
        )

        composeTestRule
            .onNodeWithText("Back")
            .assertIsDisplayed()
            .performClick()

        composeTestRule.runOnIdle {
            assertTrue(backClicked)
        }
    }

    private fun showScreen(
        onBack: (() -> Unit)? = null
    ) {
        composeTestRule.setContent {
            OreoSmartOutletTheme(
                darkTheme = false
            ) {
                DeviceConnectionSetupScreen(
                    state =
                        DeviceConnectionSetupUiState(),
                    onIpAddressChange = {},
                    onPortChange = {},
                    onRefreshDiscovery = {},
                    onDiscoveredDeviceSelected = {},
                    onTestConnection = {},
                    onSaveDevice = {},
                    onBack = onBack
                )
            }
        }
    }
}
