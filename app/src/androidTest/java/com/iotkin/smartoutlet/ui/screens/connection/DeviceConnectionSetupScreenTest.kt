package com.iotkin.smartoutlet.ui.screens.connection

import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.iotkin.smartoutlet.data.model.DeviceAddress
import com.iotkin.smartoutlet.discovery.DeviceDiscoveryUiState
import com.iotkin.smartoutlet.discovery.DiscoveredSmartOutlet
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
    fun firstTimeScreenShowsDiscoveryBeforeManualEntry() {
        showScreen()

        composeTestRule
            .onNodeWithText("Connect Your Outlet")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Nearby Smart Outlets")
            .performScrollTo()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Enter Address Manually")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Manage Device")
            .assertDoesNotExist()
    }

    @Test
    fun backIsHiddenWhenConnectionIsTheStartupScreen() {
        showScreen()

        composeTestRule
            .onNodeWithText("Back")
            .assertDoesNotExist()
    }

    @Test
    fun savedDeviceIsShownAsSavedNotVerified() {
        showScreen(
            state = savedState(),
            onBack = {}
        )

        composeTestRule
            .onNodeWithText("Manage Device")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Saved")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(
                "Saved address: 192.168.8.113:8080"
            )
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Save Changes")
            .performScrollTo()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Smart outlet verified")
            .assertDoesNotExist()
    }

    @Test
    fun backIsShownAndClickableWhenOpenedFromSettings() {
        var backClicked = false

        showScreen(
            state = savedState(),
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

    @Test
    fun successfulUserTestUsesCompactFeedback() {
        showScreen(
            state = savedState().copy(
                isDeviceVerified = true,
                hasVerifiedLiveResponse = true,
                verifiedAddress =
                    DeviceAddress(
                        host = "192.168.8.113",
                        port = 8080
                    ),
                connectionMessage =
                    "Connected to Oreo Smart Outlet."
            ),
            onBack = {}
        )

        composeTestRule
            .onNodeWithText("Connection verified")
            .performScrollTo()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(
                "Connected to Oreo Smart Outlet."
            )
            .assertIsDisplayed()
    }

    @Test
    fun findAnotherOutletRevealsDiscovery() {
        showScreen(
            state = savedState(),
            onBack = {}
        )

        composeTestRule
            .onNodeWithText("Find Another Outlet")
            .performScrollTo()
            .performClick()

        composeTestRule
            .onNodeWithText("Nearby Smart Outlets")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun discoveredOutletHasExplicitConnectAction() {
        var selected = false
        val discoveredOutlet =
            DiscoveredSmartOutlet(
                serviceName = "Oreo Smart Outlet",
                address =
                    DeviceAddress(
                        host = "192.168.8.113",
                        port = 8080
                    ),
                deviceId = "oreo-001",
                firmwareVersion = "1.0.0",
                rssi = -45
            )

        showScreen(
            state =
                DeviceConnectionSetupUiState(
                    discovery =
                        DeviceDiscoveryUiState(
                            devices =
                                listOf(
                                    discoveredOutlet
                                ),
                            hasCompletedSearchWindow =
                                true
                        )
                ),
            onDiscoveredDeviceSelected = {
                selected = true
            }
        )

        composeTestRule
            .onNodeWithText("Connect")
            .assertIsDisplayed()
            .performClick()

        composeTestRule.runOnIdle {
            assertTrue(selected)
        }
    }

    @Test
    fun forgetDeviceRequiresConfirmation() {
        var forgotDevice = false

        showScreen(
            state = savedState(),
            onBack = {},
            onDisconnectDevice = {
                forgotDevice = true
            }
        )

        composeTestRule
            .onNodeWithText("Forget Device")
            .performScrollTo()
            .performClick()

        composeTestRule
            .onNodeWithText("Forget saved device?")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Forget")
            .performClick()

        composeTestRule.runOnIdle {
            assertTrue(forgotDevice)
        }
    }

    private fun showScreen(
        state: DeviceConnectionSetupUiState =
            DeviceConnectionSetupUiState(),
        onBack: (() -> Unit)? = null,
        onDiscoveredDeviceSelected:
            (DiscoveredSmartOutlet) -> Unit = {},
        onDisconnectDevice: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            OreoSmartOutletTheme(
                darkTheme = false
            ) {
                DeviceConnectionSetupScreen(
                    state = state,
                    onIpAddressChange = {},
                    onPortChange = {},
                    onRefreshDiscovery = {},
                    onDiscoveredDeviceSelected =
                        onDiscoveredDeviceSelected,
                    onTestConnection = {},
                    onSaveDevice = {},
                    onDisconnectDevice =
                        onDisconnectDevice,
                    onBack = onBack
                )
            }
        }
    }

    private fun savedState():
            DeviceConnectionSetupUiState {
        val address =
            DeviceAddress(
                host = "192.168.8.113",
                port = 8080
            )

        return DeviceConnectionSetupUiState(
            savedAddress = address,
            friendlyDeviceName =
                "Oreo Smart Outlet",
            ipAddress = address.host,
            port = address.port.toString()
        )
    }
}
