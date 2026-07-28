package com.iotkin.smartoutlet.ui.screens.connection

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iotkin.smartoutlet.data.model.DeviceAddress
import com.iotkin.smartoutlet.discovery.DeviceDiscoveryUiState
import com.iotkin.smartoutlet.discovery.DiscoveredSmartOutlet
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
    val isRefreshing =
        discoveryState.isDiscovering ||
                discoveryState.isValidating

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = OreoShapeTokens.ExtraLarge,
        color =
            MaterialTheme.colorScheme
                .surfaceContainerLowest,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme
                .outlineVariant
                .copy(alpha = 0.3f)
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
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement =
                        Arrangement.spacedBy(
                            OreoSpacing.Base
                        )
                ) {
                    Text(
                        text = "Nearby Smart Outlets",
                        style =
                            MaterialTheme.typography
                                .headlineMedium,
                        color =
                            MaterialTheme.colorScheme
                                .onSurface
                    )

                    Text(
                        text =
                            "Search the current Wi-Fi network",
                        style =
                            MaterialTheme.typography
                                .bodyMedium,
                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onRefresh,
                    enabled = !isRefreshing,
                    modifier = Modifier.semantics {
                        contentDescription =
                            if (isRefreshing) {
                                "Refreshing nearby outlets"
                            } else {
                                "Refresh nearby outlets"
                            }
                    }
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color =
                                MaterialTheme.colorScheme
                                    .primary
                        )
                    } else {
                        Icon(
                            imageVector =
                                Icons.Filled.Refresh,
                            contentDescription = null
                        )
                    }
                }
            }

            discoveryState.errorMessage
                ?.let { message ->
                    DiscoveryMessage(
                        title =
                            "Discovery unavailable",
                        message = message,
                        isError = true
                    )
                }

            discoveryState.devices
                .forEach { device ->
                    DiscoveredDeviceCard(
                        device = device,
                        onConnect = {
                            onDeviceSelected(device)
                        }
                    )
                }

            if (discoveryState.showNoDevicesFound) {
                DiscoveryMessage(
                    title = "No smart outlets found",
                    message =
                        "Check that the outlet is powered on and connected to the same Wi-Fi network.",
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
                        "Tap refresh to look for nearby outlets."
                    },
                    style =
                        MaterialTheme.typography.bodyMedium,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DiscoveredDeviceCard(
    device: DiscoveredSmartOutlet,
    onConnect: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OreoShapeTokens.Large,
        color =
            MaterialTheme.colorScheme.primary
                .copy(alpha = 0.08f),
        border = BorderStroke(
            width = 1.dp,
            color =
                MaterialTheme.colorScheme.primary
                    .copy(alpha = 0.22f)
        )
    ) {
        Column(
            modifier = Modifier.padding(
                OreoSpacing.StackMedium
            ),
            verticalArrangement = Arrangement.spacedBy(
                OreoSpacing.Base
            )
        ) {
            Text(
                text = device.serviceName,
                style = MaterialTheme.typography.bodyLarge,
                color =
                    MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = device.address.displayAddress,
                style = MaterialTheme.typography.bodyMedium,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )

            Text(
                text =
                    "Firmware ${device.firmwareVersion} · ${device.rssi} dBm",
                style = MaterialTheme.typography.labelSmall,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )

            TextButton(
                onClick = onConnect,
                modifier =
                    Modifier.align(Alignment.End)
            ) {
                Text(
                    text = "Connect"
                )
            }
        }
    }
}

@Composable
private fun DiscoveryMessage(
    title: String,
    message: String,
    isError: Boolean
) {
    val accentColor = if (isError) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(
            OreoSpacing.Base
        )
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = accentColor
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color =
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(
    name = "Discovery With Device",
    showBackground = true,
    widthDp = 320,
    fontScale = 1.3f
)
@Composable
private fun DeviceDiscoverySectionPreview() {
    OreoSmartOutletTheme(
        darkTheme = false
    ) {
        DeviceDiscoverySection(
            discoveryState =
                DeviceDiscoveryUiState(
                    devices = listOf(
                        DiscoveredSmartOutlet(
                            serviceName =
                                "Oreo Smart Outlet",
                            address =
                                DeviceAddress(
                                    host =
                                        "192.168.8.113",
                                    port = 8080
                                ),
                            deviceId = "oreo-001",
                            firmwareVersion =
                                "1.0.0",
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
