package com.iotkin.smartoutlet.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.iotkin.smartoutlet.ui.navigation.AppDestination
import com.iotkin.smartoutlet.ui.navigation.bottomNavigationDestinations
import com.iotkin.smartoutlet.ui.navigation.isBottomNavigationRoute
import com.iotkin.smartoutlet.ui.theme.OreoSmartOutletTheme
import com.iotkin.smartoutlet.ui.theme.OreoSpacing

@Composable
fun OreoAppScaffold(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit
) {
    val backStackEntry by navController.currentBackStackEntryAsState()

    val currentRoute = backStackEntry
        ?.destination
        ?.route
        ?: AppDestination.HOME.route

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (isBottomNavigationRoute(currentRoute)) {
                OreoBottomNavigation(
                    currentRoute = currentRoute,
                    onDestinationSelected = { destination ->
                        if (currentRoute != destination.route) {
                            navController.navigate(destination.route) {
                                popUpTo(AppDestination.HOME.route) {
                                    inclusive = false
                                }

                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
        },
        content = content
    )
}

@Composable
fun OreoBottomNavigation(
    currentRoute: String,
    onDestinationSelected: (AppDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        bottomNavigationDestinations.forEach { destination ->
            val selected =
                currentRoute == destination.route

            NavigationBarItem(
                selected = selected,
                onClick = {
                    onDestinationSelected(destination)
                },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription =
                            destination
                                .iconContentDescription,
                        modifier = Modifier.size(
                            OreoSpacing.StandardIcon
                        )
                    )
                },
                label = {
                    Text(
                        text = destination.label,
                        style = MaterialTheme.typography
                            .labelSmall
                    )
                },
                modifier = Modifier.testTag(
                    "bottom_nav_${destination.route}"
                ),
                colors =
                    NavigationBarItemDefaults.colors(
                        selectedIconColor =
                            MaterialTheme.colorScheme
                                .primary,
                        selectedTextColor =
                            MaterialTheme.colorScheme
                                .primary,
                        indicatorColor =
                            MaterialTheme.colorScheme
                                .primary
                                .copy(alpha = 0.12f),
                        unselectedIconColor =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant,
                        unselectedTextColor =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )
            )
        }
    }
}

@Preview(
    name = "Bottom Navigation Light",
    showBackground = true
)
@Composable
private fun BottomNavigationLightPreview() {
    OreoSmartOutletTheme(
        darkTheme = false
    ) {
        OreoBottomNavigation(
            currentRoute = AppDestination.HOME.route,
            onDestinationSelected = {}
        )
    }
}

@Preview(
    name = "Bottom Navigation Dark",
    showBackground = true,
    backgroundColor = 0xFF0F172A
)
@Composable
private fun BottomNavigationDarkPreview() {
    OreoSmartOutletTheme(
        darkTheme = true
    ) {
        OreoBottomNavigation(
            currentRoute = AppDestination.DIAGNOSTICS.route,
            onDestinationSelected = {}
        )
    }
}

@Preview(
    name = "Bottom Navigation Large Font",
    widthDp = 360,
    fontScale = 2f,
    showBackground = true,
    backgroundColor = 0xFF0F172A
)
@Composable
private fun BottomNavigationLargeFontPreview() {
    OreoSmartOutletTheme(
        darkTheme = true
    ) {
        OreoBottomNavigation(
            currentRoute =
                AppDestination.DIAGNOSTICS.route,
            onDestinationSelected = {}
        )
    }
}
