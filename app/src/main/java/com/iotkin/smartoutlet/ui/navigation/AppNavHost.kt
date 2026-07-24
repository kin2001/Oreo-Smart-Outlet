package com.iotkin.smartoutlet.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

private object Routes {
    const val HOME = "home"
    const val SCHEDULE = "schedule"
    const val DIAGNOSTICS = "diagnostics"
    const val SETTINGS = "settings"
}

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            PlaceholderScreen(
                title = "Home",
                navController = navController
            )
        }

        composable(Routes.SCHEDULE) {
            PlaceholderScreen(
                title = "Schedule",
                navController = navController
            )
        }

        composable(Routes.DIAGNOSTICS) {
            PlaceholderScreen(
                title = "Diagnostics",
                navController = navController
            )
        }

        composable(Routes.SETTINGS) {
            PlaceholderScreen(
                title = "Settings",
                navController = navController
            )
        }
    }
}

@Composable
private fun PlaceholderScreen(
    title: String,
    navController: NavHostController
) {
    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium
                )

                Button(onClick = { navigateTo(navController, Routes.HOME) }) {
                    Text("Home")
                }

                Button(onClick = { navigateTo(navController, Routes.SCHEDULE) }) {
                    Text("Schedule")
                }

                Button(onClick = { navigateTo(navController, Routes.DIAGNOSTICS) }) {
                    Text("Diagnostics")
                }

                Button(onClick = { navigateTo(navController, Routes.SETTINGS) }) {
                    Text("Settings")
                }
            }
        }
    }
}

private fun navigateTo(
    navController: NavHostController,
    route: String
) {
    navController.navigate(route) {
        popUpTo(Routes.HOME) {
            saveState = true
        }

        launchSingleTop = true
        restoreState = true
    }
}