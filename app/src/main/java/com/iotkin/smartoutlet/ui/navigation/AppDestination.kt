package com.iotkin.smartoutlet.ui.navigation

enum class AppDestination(
    val route: String,
    val label: String,
    val iconText: String
) {
    HOME(
        route = "home",
        label = "Home",
        iconText = "H"
    ),
    SCHEDULE(
        route = "schedule",
        label = "Schedule",
        iconText = "S"
    ),
    DIAGNOSTICS(
        route = "diagnostics",
        label = "Diagnostics",
        iconText = "D"
    ),
    SETTINGS(
        route = "settings",
        label = "Settings",
        iconText = "⚙"
    )
}

val bottomNavigationDestinations = listOf(
    AppDestination.HOME,
    AppDestination.SCHEDULE,
    AppDestination.DIAGNOSTICS,
    AppDestination.SETTINGS
)