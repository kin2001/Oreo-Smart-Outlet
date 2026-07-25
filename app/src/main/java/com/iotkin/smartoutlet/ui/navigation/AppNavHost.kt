package com.iotkin.smartoutlet.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.iotkin.smartoutlet.ui.components.OreoAppScaffold
import com.iotkin.smartoutlet.ui.screens.connection.DeviceConnectionSetupRoute
import androidx.lifecycle.viewmodel.compose.viewModel
import com.iotkin.smartoutlet.ui.screens.diagnostics.DiagnosticsViewModel
import com.iotkin.smartoutlet.ui.screens.diagnostics.ForegroundStatusPollingEffect
import com.iotkin.smartoutlet.ui.screens.diagnostics.DiagnosticsRoute

private object RootRoutes {
    const val CONNECTION = "connection"
    const val MAIN = "main"
}

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = RootRoutes.CONNECTION
    ) {
        composable(
            route = RootRoutes.CONNECTION
        ) {
            DeviceConnectionSetupRoute(
                onDeviceSaved = {
                    navController.navigate(
                        RootRoutes.MAIN
                    ) {
                        popUpTo(
                            RootRoutes.CONNECTION
                        ) {
                            inclusive = true
                        }

                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = RootRoutes.MAIN
        ) {
            MainAppNavHost(
                onRunDiscoveryAgain = {
                    navController.navigate(
                        RootRoutes.CONNECTION
                    ) {
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

@Composable
private fun MainAppNavHost(
    onRunDiscoveryAgain: () -> Unit,
    navController: NavHostController =
        rememberNavController()
) {
    val diagnosticsViewModel:
            DiagnosticsViewModel = viewModel()

    ForegroundStatusPollingEffect(
        viewModel = diagnosticsViewModel
    )
    OreoAppScaffold(
        navController = navController
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.HOME.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(
                route = AppDestination.HOME.route
            ) {
                DestinationPlaceholder(
                    title = "Home"
                )
            }

            composable(
                route = AppDestination.SCHEDULE.route
            ) {
                DestinationPlaceholder(
                    title = "Schedule"
                )
            }

            composable(
                route = AppDestination.DIAGNOSTICS.route
            ) {
                DiagnosticsRoute(
                    viewModel = diagnosticsViewModel,
                    onRunDiscoveryAgain =
                        onRunDiscoveryAgain
                )
            }

            composable(
                route = AppDestination.SETTINGS.route
            ) {
                DestinationPlaceholder(
                    title = "Settings"
                )
            }
        }
    }
}

@Composable
private fun DestinationPlaceholder(
    title: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}