package com.iotkin.smartoutlet.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.iotkin.smartoutlet.data.model.RelayNumber
import com.iotkin.smartoutlet.data.settings.DeviceSettingsStore
import com.iotkin.smartoutlet.ui.components.OreoAppScaffold
import com.iotkin.smartoutlet.ui.screens.connection.DeviceConnectionSetupRoute
import com.iotkin.smartoutlet.ui.screens.diagnostics.DiagnosticsRoute
import com.iotkin.smartoutlet.ui.screens.diagnostics.DiagnosticsViewModel
import com.iotkin.smartoutlet.ui.screens.diagnostics.ForegroundStatusPollingEffect
import com.iotkin.smartoutlet.ui.screens.home.HomeRoute
import com.iotkin.smartoutlet.ui.screens.home.OutletDetailsRoute
import com.iotkin.smartoutlet.ui.screens.schedule.EditScheduleRoute
import com.iotkin.smartoutlet.ui.screens.schedule.ScheduleOverviewRoute
import com.iotkin.smartoutlet.ui.screens.settings.AboutScreen
import com.iotkin.smartoutlet.ui.screens.settings.SettingsRoute
import kotlinx.coroutines.flow.first

private object RootRoutes {
    const val CONNECTION = "connection"
    const val MAIN = "main"
}

private object MainRoutes {
    const val OUTLET_ARGUMENT =
        "outletNumber"

    const val OUTLET_DETAILS =
        "outlet/{$OUTLET_ARGUMENT}"

    const val SCHEDULE_OUTLET_ARGUMENT =
        "scheduleOutletNumber"

    const val EDIT_SCHEDULE =
        "schedule/edit/{$SCHEDULE_OUTLET_ARGUMENT}"

    const val ABOUT = "about"

    fun outletDetails(
        relay: RelayNumber
    ): String {
        return "outlet/${relay.apiValue}"
    }

    fun editSchedule(
        relay: RelayNumber
    ): String {
        return "schedule/edit/${relay.apiValue}"
    }
}

private enum class StartupDestination {
    LOADING,
    CONNECTION,
    MAIN
}

@Composable
fun AppNavHost(
    navController: NavHostController =
        rememberNavController()
) {
    val context = LocalContext.current

    val settingsStore =
        remember(context) {
            DeviceSettingsStore(
                context.applicationContext
            )
        }

    val startupDestination by
    produceState(
        initialValue =
            StartupDestination.LOADING,
        key1 = settingsStore
    ) {
        val savedAddress =
            settingsStore
                .savedDeviceAddress
                .first()

        value =
            if (savedAddress == null) {
                StartupDestination
                    .CONNECTION
            } else {
                StartupDestination.MAIN
            }
    }

    when (startupDestination) {
        StartupDestination.LOADING -> {
            StartupLoadingScreen()
        }

        StartupDestination.CONNECTION,
        StartupDestination.MAIN -> {
            RootNavigationHost(
                navController =
                    navController,
                startDestination =
                    if (
                        startupDestination ==
                        StartupDestination.MAIN
                    ) {
                        RootRoutes.MAIN
                    } else {
                        RootRoutes.CONNECTION
                    }
            )
        }
    }
}

@Composable
private fun RootNavigationHost(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination =
            startDestination
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
                },
                onDeviceDisconnected = {
                    navController.navigate(
                        RootRoutes.CONNECTION
                    ) {
                        popUpTo(
                            RootRoutes.MAIN
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
                onManageDevice = {
                    navController.navigate(
                        RootRoutes.CONNECTION
                    ) {
                        launchSingleTop = true
                    }
                },
                onSavedDeviceRemoved = {
                    navController.navigate(
                        RootRoutes.CONNECTION
                    ) {
                        popUpTo(
                            RootRoutes.MAIN
                        ) {
                            inclusive = true
                        }

                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

@Composable
private fun MainAppNavHost(
    onManageDevice: () -> Unit,
    onSavedDeviceRemoved: () -> Unit,
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
                    onOutletSelected = { relay ->
                        navController.navigate(
                            MainRoutes
                                .outletDetails(
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
                            MainRoutes
                                .editSchedule(
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
                        navController
                            .popBackStack()
                    }
                )
            }

            composable(
                route =
                    AppDestination
                        .DIAGNOSTICS.route
            ) {
                DiagnosticsRoute(
                    viewModel =
                        diagnosticsViewModel,
                    onRunDiscoveryAgain =
                        onManageDevice
                )
            }

            composable(
                route =
                    AppDestination.SETTINGS.route
            ) {
                SettingsRoute(
                    onManageDevice =
                        onManageDevice,
                    onDeviceRemoved =
                        onSavedDeviceRemoved,
                    onAbout = {
                        navController.navigate(
                            MainRoutes.ABOUT
                        ) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(
                route = MainRoutes.ABOUT
            ) {
                val statusState by
                    diagnosticsViewModel
                        .statusState
                        .collectAsStateWithLifecycle()

                AboutScreen(
                    firmwareVersion =
                        statusState.status
                            ?.firmwareVersion,
                    onBack = {
                        navController
                            .popBackStack()
                    }
                )
            }

            composable(
                route =
                    MainRoutes.OUTLET_DETAILS,
                arguments = listOf(
                    navArgument(
                        MainRoutes
                            .OUTLET_ARGUMENT
                    ) {
                        type = NavType.IntType
                    }
                )
            ) { backStackEntry ->
                val relayNumber =
                    backStackEntry.arguments
                        ?.getInt(
                            MainRoutes
                                .OUTLET_ARGUMENT
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
private fun StartupLoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
