package com.iotkin.smartoutlet.ui.screens.connection

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iotkin.smartoutlet.data.model.DeviceAddress
import com.iotkin.smartoutlet.discovery.DeviceDiscoveryUiState
import com.iotkin.smartoutlet.discovery.DiscoveredSmartOutlet
import com.iotkin.smartoutlet.ui.components.OreoSecondaryButton
import com.iotkin.smartoutlet.ui.theme.OreoShapeTokens
import com.iotkin.smartoutlet.ui.theme.OreoSmartOutletTheme
import com.iotkin.smartoutlet.ui.theme.OreoSpacing

@Composable
fun DeviceDiscoverySection(
    discoveryState: DeviceDiscoveryUiState,
    onRefresh: () -> Unit,
    onDeviceSelected: (DiscoveredSmartOutlet) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = OreoShapeTokens.ExtraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(
                alpha = 0.3f
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(
                OreoSpacing.CardPadding
            ),
            verticalArrangement = Arrangement.spacedBy(
                OreoSpacing.StackMedium
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(
                        OreoSpacing.Base
                    )
                ) {
                    Text(
                        text = "Nearby Smart Outlets",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Searching the current Wi-Fi network",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (
                    discoveryState.isDiscovering ||
                    discoveryState.isValidating
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            discoveryState.errorMessage?.let { message ->
                DiscoveryMessageCard(
                    title = "Discovery unavailable",
                    message = message,
                    isError = true
                )
            }

            discoveryState.devices.forEach { device ->
                DiscoveredDeviceCard(
                    device = device,
                    onClick = {
                        onDeviceSelected(device)
                    }
                )
            }

            if (discoveryState.showNoDevicesFound) {
                DiscoveryMessageCard(
                    title = "No smart outlets found",
                    message = "Check that the ESP8266 is powered on and connected to the same Wi-Fi network.",
                    isError = false
                )
            }

            if (
                discoveryState.devices.isEmpty() &&
                !discoveryState.showNoDevicesFound &&
                discoveryState.errorMessage == null
            ) {
                Text(
                    text = if (
                        discoveryState.isDiscovering ||
                        discoveryState.isValidating
                    ) {
                        "Looking for Oreo Smart Outlet devices..."
                    } else {
                        "Start a new search to look for nearby devices."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OreoSecondaryButton(
                text = "Refresh Discovery",
                onClick = onRefresh,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun DiscoveredDeviceCard(
    device: DiscoveredSmartOutlet,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = OreoShapeTokens.Large,
        color = MaterialTheme.colorScheme.primary.copy(
            alpha = 0.08f
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(
                alpha = 0.22f
            )
        )
    ) {
        Row(
            modifier = Modifier.padding(
                OreoSpacing.StackMedium
            ),
            horizontalArrangement = Arrangement.spacedBy(
                OreoSpacing.StackMedium
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(
                            alpha = 0.14f
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "O",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(
                    OreoSpacing.Base
                )
            ) {
                Text(
                    text = device.serviceName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = device.address.displayAddress,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Firmware ${device.firmwareVersion}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(
                    OreoSpacing.Base
                )
            ) {
                Text(
                    text = "${device.rssi} dBm",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Select",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun DiscoveryMessageCard(
    title: String,
    message: String,
    isError: Boolean
) {
    val accentColor = if (isError) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OreoShapeTokens.Large,
        color = accentColor.copy(
            alpha = 0.08f
        )
    ) {
        Column(
            modifier = Modifier.padding(
                OreoSpacing.StackMedium
            ),
            verticalArrangement = Arrangement.spacedBy(
                OreoSpacing.StackSmall
            )
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(
    name = "Discovery With Device",
    showBackground = true,
    widthDp = 412
)
@Composable
private fun DeviceDiscoverySectionPreview() {
    OreoSmartOutletTheme(
        darkTheme = false
    ) {
        DeviceDiscoverySection(
            discoveryState = DeviceDiscoveryUiState(
                devices = listOf(
                    DiscoveredSmartOutlet(
                        serviceName = "Oreo Smart Outlet",
                        address = DeviceAddress(
                            host = "192.168.8.113",
                            port = 8080
                        ),
                        deviceId = "oreo-001",
                        firmwareVersion = "1.0.0",
                        rssi = -55
                    )
                ),
                hasCompletedSearchWindow = true
            ),
            onRefresh = {},
            onDeviceSelected = {},
            modifier = Modifier.padding(
                OreoSpacing.ScreenMargin
            )
        )
    }
}

@Preview(
    name = "No Devices Found",
    showBackground = true,
    widthDp = 412
)
@Composable
private fun NoDevicesFoundPreview() {
    OreoSmartOutletTheme(
        darkTheme = false
    ) {
        DeviceDiscoverySection(
            discoveryState = DeviceDiscoveryUiState(
                hasCompletedSearchWindow = true
            ),
            onRefresh = {},
            onDeviceSelected = {},
            modifier = Modifier.padding(
                OreoSpacing.ScreenMargin
            )
        )
    }
}