package com.iotkin.smartoutlet.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iotkin.smartoutlet.ui.navigation.AppDestination
import com.iotkin.smartoutlet.ui.theme.OreoSmartOutletTheme
import com.iotkin.smartoutlet.ui.theme.OreoSpacing

@Composable
private fun ComponentGalleryContent() {
    Surface(
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(
                    OreoSpacing.ScreenMargin
                ),
            verticalArrangement = Arrangement.spacedBy(
                OreoSpacing.StackLarge
            )
        ) {
            Text(
                text = "Oreo Components",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            OreoCatLogo(
                size = 140.dp
            )

            Text(
                text = "Connection",
                style = MaterialTheme.typography.headlineMedium
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(
                    OreoSpacing.StackSmall
                )
            ) {
                ConnectionBadge(
                    status = ConnectionStatus.Online
                )

                ConnectionBadge(
                    status = ConnectionStatus.Offline
                )
            }

            ConnectionBadge(
                status = ConnectionStatus.Reconnecting
            )

            Text(
                text = "Buttons",
                style = MaterialTheme.typography.headlineMedium
            )

            OreoPrimaryButton(
                text = "Save Changes",
                onClick = {},
                modifier = Modifier.fillMaxWidth()
            )

            OreoSecondaryButton(
                text = "Refresh Status",
                onClick = {},
                modifier = Modifier.fillMaxWidth()
            )

            OreoPrimaryButton(
                text = "Connecting",
                onClick = {},
                isLoading = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Relay",
                style = MaterialTheme.typography.headlineMedium
            )

            RelayCard(
                outletLabel = "Outlet 1",
                outletName = "Living Room",
                isOn = true,
                scheduleSummary =
                    "8:00 PM – 5:00 AM",
                lastUpdated = "Updated 4:52 PM",
                onToggle = {},
                onEditName = {},
                onOpenDetails = {}
            )

            RelayCard(
                outletLabel = "Outlet 2",
                outletName = "Bedroom",
                isOn = false,
                scheduleSummary =
                    "1:42 PM – 1:43 PM",
                lastUpdated = "Updated 4:52 PM",
                onToggle = {},
                onEditName = {},
                onOpenDetails = {}
            )

            Text(
                text = "Schedule",
                style = MaterialTheme.typography.headlineMedium
            )

            ScheduleCard(
                outletLabel = "Outlet 1",
                outletName = "Living Room",
                enabled = true,
                onEnabledChange = {},
                onTime = "06:00 PM",
                offTime = "06:00 AM",
                nextEvent = "Today, 6:00 PM",
                onEditSchedule = {},
                iconContent = {
                    Text(
                        text = "1",
                        color = MaterialTheme.colorScheme
                            .onSecondaryContainer
                    )
                }
            )

            Text(
                text = "Status",
                style = MaterialTheme.typography.headlineMedium
            )

            StatusRow(
                label = "Wi-Fi Signal",
                value = "-55 dBm"
            )

            OreoLoadingState(
                message = "Refreshing device status"
            )

            OreoStaleState(
                lastUpdatedText = "3 minutes ago",
                onRefresh = {}
            )

            OreoOfflineState(
                onRetry = {}
            )

            Text(
                text = "Bottom Navigation",
                style = MaterialTheme.typography.headlineMedium
            )

            OreoBottomNavigation(
                currentRoute = AppDestination.HOME.route,
                onDestinationSelected = {}
            )
        }
    }
}

@Preview(
    name = "Component Gallery Light",
    showBackground = true,
    widthDp = 412,
    heightDp = 1800
)
@Composable
private fun ComponentGalleryLightPreview() {
    OreoSmartOutletTheme(
        darkTheme = false
    ) {
        ComponentGalleryContent()
    }
}

@Preview(
    name = "Component Gallery Dark",
    showBackground = true,
    backgroundColor = 0xFF0F172A,
    widthDp = 412,
    heightDp = 1800
)
@Composable
private fun ComponentGalleryDarkPreview() {
    OreoSmartOutletTheme(
        darkTheme = true
    ) {
        ComponentGalleryContent()
    }
}
