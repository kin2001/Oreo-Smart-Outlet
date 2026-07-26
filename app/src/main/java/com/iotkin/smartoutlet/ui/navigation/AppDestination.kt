package com.iotkin.smartoutlet.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val iconContentDescription: String = label
) {
    HOME(
        route = "home",
        label = "Home",
        icon = Icons.Filled.Home
    ),
    SCHEDULE(
        route = "schedule",
        label = "Schedule",
        icon = Icons.Filled.DateRange
    ),
    DIAGNOSTICS(
        route = "diagnostics",
        label = "Status",
        icon = Icons.Filled.Info,
        iconContentDescription = "Diagnostics"
    ),
    SETTINGS(
        route = "settings",
        label = "Settings",
        icon = Icons.Filled.Settings
    )
}

val bottomNavigationDestinations = listOf(
    AppDestination.HOME,
    AppDestination.SCHEDULE,
    AppDestination.DIAGNOSTICS,
    AppDestination.SETTINGS
)

internal fun isBottomNavigationRoute(
    route: String?
): Boolean {
    return bottomNavigationDestinations.any {
        it.route == route
    }
}
