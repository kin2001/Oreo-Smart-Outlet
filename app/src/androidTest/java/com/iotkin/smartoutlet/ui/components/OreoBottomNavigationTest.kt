package com.iotkin.smartoutlet.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.iotkin.smartoutlet.ui.navigation.AppDestination
import com.iotkin.smartoutlet.ui.theme.OreoSmartOutletTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OreoBottomNavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun bottomNavigationDisplaysAllDestinations() {
        composeTestRule.setContent {
            OreoSmartOutletTheme(
                darkTheme = false
            ) {
                OreoBottomNavigation(
                    currentRoute = AppDestination.HOME.route,
                    onDestinationSelected = {}
                )
            }
        }

        composeTestRule
            .onNodeWithTag("bottom_nav_home")
            .assertIsDisplayed()
            .assertIsSelected()

        composeTestRule
            .onNodeWithTag("bottom_nav_schedule")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("bottom_nav_diagnostics")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("bottom_nav_settings")
            .assertIsDisplayed()

        listOf(
            "Home",
            "Schedule",
            "Diagnostics",
            "Settings"
        ).forEach { label ->
            composeTestRule
                .onNodeWithContentDescription(label)
                .assertIsDisplayed()
        }
    }

    @Test
    fun clickingDiagnosticsReportsSelection() {
        var selectedDestination: AppDestination? = null

        composeTestRule.setContent {
            OreoSmartOutletTheme(
                darkTheme = false
            ) {
                OreoBottomNavigation(
                    currentRoute = AppDestination.HOME.route,
                    onDestinationSelected = { destination ->
                        selectedDestination = destination
                    }
                )
            }
        }

        composeTestRule
            .onNodeWithTag("bottom_nav_diagnostics")
            .performClick()

        composeTestRule.runOnIdle {
            assertEquals(
                AppDestination.DIAGNOSTICS,
                selectedDestination
            )
        }
    }
}
