package com.iotkin.smartoutlet.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.iotkin.smartoutlet.data.model.RelayNumber
import com.iotkin.smartoutlet.ui.components.OreoAppScaffold
import com.iotkin.smartoutlet.ui.screens.connection.DeviceConnectionSetupRoute
import com.iotkin.smartoutlet.ui.screens.diagnostics.DiagnosticsRoute
import com.iotkin.smartoutlet.ui.screens.diagnostics.DiagnosticsViewModel
import com.iotkin.smartoutlet.ui.screens.diagnostics.ForegroundStatusPollingEffect
import com.iotkin.smartoutlet.ui.screens.home.HomeRoute
import com.iotkin.smartoutlet.ui.screens.home.OutletDetailsRoute
import com.iotkin.smartoutlet.ui.screens.schedule.EditScheduleRoute
import com.iotkin.smartoutlet.ui.screens.schedule.ScheduleOverviewRoute

private object RootRoutes {
    const val CONNECTION = "connection"
    const val MAIN = "main"
}

private object MainRoutes {
    const val OUTLET_ARGUMENT =
        "outletNumber"

    const val OUTLET_DETAILS =
        "outlet/{$OUTLET_ARGUMENT}"

    fun outletDetails(
        relay: RelayNumber
    ): String {
        return "outlet/${relay.apiValue}"
    }
    const val SCHEDULE_OUTLET_ARGUMENT =
        "scheduleOutletNumber"

    const val EDIT_SCHEDULE =
        "schedule/edit/{$SCHEDULE_OUTLET_ARGUMENT}"

    fun editSchedule(
        relay: RelayNumber
    ): String {
        return "schedule/edit/${relay.apiValue}"
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController =
        rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination =
            RootRoutes.CONNECTION
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
            startDestination =
                AppDestination.HOME.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(
                route =
                    AppDestination.HOME.route
            ) {
                HomeRoute(
                    viewModel =
                        diagnosticsViewModel,
                    onOutletSelected = {
                            relay ->
                        navController.navigate(
                            MainRoutes.outletDetails(
                                relay
                            )
                        )
                    }
                )
            }

            composable(
                route =
                    AppDestination.SCHEDULE.route
            ) {
                ScheduleOverviewRoute(
                    viewModel =
                        diagnosticsViewModel,
                    onEditSchedule = { relay ->
                        navController.navigate(
                            MainRoutes.editSchedule(
                                relay
                            )
                        )
                    }
                )
            }

            composable(
                route =
                    MainRoutes.EDIT_SCHEDULE,
                arguments = listOf(
                    navArgument(
                        MainRoutes
                            .SCHEDULE_OUTLET_ARGUMENT
                    ) {
                        type = NavType.IntType
                    }
                )
            ) { backStackEntry ->
                val relayNumber =
                    backStackEntry.arguments
                        ?.getInt(
                            MainRoutes
                                .SCHEDULE_OUTLET_ARGUMENT
                        )
                        ?: 1

                val relay =
                    if (relayNumber == 2) {
                        RelayNumber.RELAY_2
                    } else {
                        RelayNumber.RELAY_1
                    }

                EditScheduleRoute(
                    relay = relay,
                    viewModel =
                        diagnosticsViewModel,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route =
                    AppDestination.DIAGNOSTICS.route
            ) {
                DiagnosticsRoute(
                    viewModel =
                        diagnosticsViewModel,
                    onRunDiscoveryAgain =
                        onRunDiscoveryAgain
                )
            }

            composable(
                route =
                    AppDestination.SETTINGS.route
            ) {
                DestinationPlaceholder(
                    title = "Settings"
                )
            }

            composable(
                route =
                    MainRoutes.OUTLET_DETAILS,
                arguments = listOf(
                    navArgument(
                        MainRoutes.OUTLET_ARGUMENT
                    ) {
                        type =
                            NavType.IntType
                    }
                )
            ) { backStackEntry ->
                val relayNumber =
                    backStackEntry.arguments
                        ?.getInt(
                            MainRoutes.OUTLET_ARGUMENT
                        )
                        ?: 1

                val relay =
                    if (relayNumber == 2) {
                        RelayNumber.RELAY_2
                    } else {
                        RelayNumber.RELAY_1
                    }

                OutletDetailsRoute(
                    relay = relay,
                    viewModel =
                        diagnosticsViewModel,
                    onBack = {
                        navController
                            .popBackStack()
                    }
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
            style =
                MaterialTheme.typography
                    .headlineLarge,
            color =
                MaterialTheme.colorScheme
                    .onBackground
        )
    }
}