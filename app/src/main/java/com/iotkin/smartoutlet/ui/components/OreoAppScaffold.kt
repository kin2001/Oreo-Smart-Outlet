package com.iotkin.smartoutlet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.iotkin.smartoutlet.ui.navigation.AppDestination
import com.iotkin.smartoutlet.ui.navigation.bottomNavigationDestinations
import com.iotkin.smartoutlet.ui.theme.OreoShapeTokens
import com.iotkin.smartoutlet.ui.theme.OreoSmartOutletTheme
import com.iotkin.smartoutlet.ui.theme.OreoSpacing
import androidx.compose.ui.platform.testTag

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
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 8.dp
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        MaterialTheme.colorScheme.outlineVariant.copy(
                            alpha = 0.3f
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        OreoSpacing.BottomNavigationHeight
                    )
            ) {
                bottomNavigationDestinations.forEach { destination ->
                    val selected =
                        currentRoute == destination.route

                    BottomNavigationDestination(
                        destination = destination,
                        selected = selected,
                        onClick = {
                            onDestinationSelected(destination)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavigationDestination(
    destination: AppDestination,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .testTag(
                "bottom_nav_${destination.route}"
            )
            .selectable(
                selected = selected,
                role = Role.Tab,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(
                    width = 40.dp,
                    height = 28.dp
                )
                .background(
                    color = if (selected) {
                        MaterialTheme.colorScheme.primary.copy(
                            alpha = 0.12f
                        )
                    } else {
                        Color.Transparent
                    },
                    shape = OreoShapeTokens.Pill
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = destination.iconText,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor
            )
        }

        Text(
            text = destination.label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor
        )
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