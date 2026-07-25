package com.iotkin.smartoutlet.ui.screens.connection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.iotkin.smartoutlet.ui.components.ConnectionBadge
import com.iotkin.smartoutlet.ui.components.ConnectionStatus
import com.iotkin.smartoutlet.ui.components.OreoCatLogo
import com.iotkin.smartoutlet.ui.components.OreoPrimaryButton
import com.iotkin.smartoutlet.ui.components.OreoSecondaryButton
import com.iotkin.smartoutlet.ui.theme.OreoShapeTokens
import com.iotkin.smartoutlet.ui.theme.OreoSmartOutletTheme
import com.iotkin.smartoutlet.ui.theme.OreoSpacing
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect

@Composable
fun DeviceConnectionSetupScreen(
    state: DeviceConnectionSetupUiState,
    onIpAddressChange: (String) -> Unit,
    onPortChange: (String) -> Unit,
    onTestConnection: () -> Unit,
    onSaveDevice: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(
                    horizontal = OreoSpacing.ScreenMargin,
                    vertical = OreoSpacing.StackLarge
                ),
            verticalArrangement = Arrangement.spacedBy(
                OreoSpacing.StackLarge
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(
                    OreoSpacing.StackMedium
                )
            ) {
                OreoCatLogo()

                Text(
                    text = "Connect Your Outlet",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Enter the IP address and API port shown by your ESP8266.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            ConnectionAddressForm(
                state = state,
                onIpAddressChange = onIpAddressChange,
                onPortChange = onPortChange
            )

            ConnectionHelpCard()

            when {
                state.isDeviceVerified -> {
                    VerifiedDeviceCard(
                        message = state.connectionMessage
                            ?: "SmartOutlet_ESP8266 responded successfully."
                    )
                }

                state.connectionIssue != null -> {
                    ConnectionIssueCard(
                        issue = state.connectionIssue,
                        message = state.connectionMessage
                            ?: "Unable to connect to the smart outlet."
                    )
                }
            }

            state.saveError?.let { message ->
                SaveDeviceErrorCard(
                    message = message
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(
                    OreoSpacing.StackMedium
                )
            ) {
                OreoSecondaryButton(
                    text = if (state.isTestingConnection) {
                        "Testing Connection"
                    } else {
                        "Test Connection"
                    },
                    onClick = onTestConnection,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isTestingConnection,
                    isLoading = state.isTestingConnection
                )

                OreoPrimaryButton(
                    text = if (state.isSavingDevice) {
                        "Saving Device"
                    } else {
                        "Save Device"
                    },
                    onClick = onSaveDevice,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.isDeviceVerified &&
                            !state.isTestingConnection &&
                            !state.isSavingDevice,
                    isLoading = state.isSavingDevice
                )
            }
        }
    }
}

@Composable
private fun ConnectionAddressForm(
    state: DeviceConnectionSetupUiState,
    onIpAddressChange: (String) -> Unit,
    onPortChange: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OreoShapeTokens.ExtraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(
                OreoSpacing.CardPadding
            ),
            verticalArrangement = Arrangement.spacedBy(
                OreoSpacing.StackMedium
            )
        ) {
            Text(
                text = "Device Address",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            OutlinedTextField(
                value = state.ipAddress,
                onValueChange = onIpAddressChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(
                        text = "IP Address"
                    )
                },
                placeholder = {
                    Text(
                        text = "192.168.1.100"
                    )
                },
                supportingText = state.ipError?.let { error ->
                    {
                        Text(
                            text = error
                        )
                    }
                },
                isError = state.ipError != null,
                singleLine = true,
                shape = OreoShapeTokens.Medium,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                )
            )

            OutlinedTextField(
                value = state.port,
                onValueChange = onPortChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(
                        text = "API Port"
                    )
                },
                placeholder = {
                    Text(
                        text = "8080"
                    )
                },
                supportingText = state.portError?.let { error ->
                    {
                        Text(
                            text = error
                        )
                    }
                },
                isError = state.portError != null,
                singleLine = true,
                shape = OreoShapeTokens.Medium,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                )
            )
        }
    }
}

@Composable
private fun ConnectionHelpCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OreoShapeTokens.Large,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier.padding(
                OreoSpacing.CardPadding
            ),
            verticalArrangement = Arrangement.spacedBy(
                OreoSpacing.StackSmall
            )
        ) {
            Text(
                text = "Connection Requirements",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Your phone and smart outlet must be connected to the same Wi-Fi network.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "The default API port is 8080.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun VerifiedDeviceCard(
    message: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OreoShapeTokens.Large,
        color = MaterialTheme.colorScheme.primary.copy(
            alpha = 0.08f
        )
    ) {
        Row(
            modifier = Modifier.padding(
                OreoSpacing.CardPadding
            ),
            horizontalArrangement = Arrangement.spacedBy(
                OreoSpacing.StackMedium
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(
                    OreoSpacing.Base
                )
            ) {
                Text(
                    text = "Smart outlet verified",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            ConnectionBadge(
                status = ConnectionStatus.Online
            )
        }
    }
}

@Composable
private fun ConnectionIssueCard(
    issue: ConnectionIssueType,
    message: String
) {
    val title: String
    val accentColor: androidx.compose.ui.graphics.Color

    when (issue) {
        ConnectionIssueType.TIMEOUT -> {
            title = "Connection timed out"
            accentColor = androidx.compose.ui.graphics.Color(
                0xFFF59E0B
            )
        }

        ConnectionIssueType.INVALID_DEVICE -> {
            title = "Incompatible device"
            accentColor = MaterialTheme.colorScheme.error
        }

        ConnectionIssueType.CONNECTION_FAILED -> {
            title = "Connection failed"
            accentColor = MaterialTheme.colorScheme.error
        }
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
                OreoSpacing.CardPadding
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

@Composable
private fun SaveDeviceErrorCard(
    message: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OreoShapeTokens.Large,
        color = MaterialTheme.colorScheme.error.copy(
            alpha = 0.08f
        )
    ) {
        Column(
            modifier = Modifier.padding(
                OreoSpacing.CardPadding
            ),
            verticalArrangement = Arrangement.spacedBy(
                OreoSpacing.StackSmall
            )
        ) {
            Text(
                text = "Unable to save device",
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

@Composable
fun DeviceConnectionSetupRoute(
    onDeviceSaved: () -> Unit,
    modifier: Modifier = Modifier,
    setupViewModel: DeviceConnectionSetupViewModel = viewModel()
) {
    val state by setupViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(setupViewModel) {
        setupViewModel.events.collect { event ->
            when (event) {
                DeviceConnectionSetupEvent.DeviceSaved -> {
                    onDeviceSaved()
                }
            }
        }
    }

    DeviceConnectionSetupScreen(
        state = state,
        onIpAddressChange = setupViewModel::onIpAddressChange,
        onPortChange = setupViewModel::onPortChange,
        onTestConnection = setupViewModel::testConnection,
        onSaveDevice = setupViewModel::saveDevice,
        modifier = modifier
    )
}

@Preview(
    name = "Connection Setup Light",
    showBackground = true,
    widthDp = 412,
    heightDp = 900
)
@Composable
private fun DeviceConnectionSetupLightPreview() {
    OreoSmartOutletTheme(
        darkTheme = false
    ) {
        DeviceConnectionSetupScreen(
            state = DeviceConnectionSetupUiState(
                ipAddress = "192.168.1.100"
            ),
            onIpAddressChange = {},
            onPortChange = {},
            onTestConnection = {},
            onSaveDevice = {}
        )
    }
}

@Preview(
    name = "Verified Device Dark",
    showBackground = true,
    backgroundColor = 0xFF0F172A,
    widthDp = 412,
    heightDp = 900
)
@Composable
private fun VerifiedDeviceDarkPreview() {
    OreoSmartOutletTheme(
        darkTheme = true
    ) {
        DeviceConnectionSetupScreen(
            state = DeviceConnectionSetupUiState(
                ipAddress = "192.168.1.100",
                port = "8080",
                isDeviceVerified = true
            ),
            onIpAddressChange = {},
            onPortChange = {},
            onTestConnection = {},
            onSaveDevice = {}
        )
    }
}

@Preview(
    name = "Connection Errors Light",
    showBackground = true,
    widthDp = 412,
    heightDp = 900
)
@Composable
private fun DeviceConnectionErrorsLightPreview() {
    OreoSmartOutletTheme(
        darkTheme = false
    ) {
        DeviceConnectionSetupScreen(
            state = DeviceConnectionSetupUiState(
                ipAddress = "192.168.1.999",
                port = "70000",
                ipError = "Each IP section must be between 0 and 255",
                portError = "The port must be between 1 and 65535"
            ),
            onIpAddressChange = {},
            onPortChange = {},
            onTestConnection = {},
            onSaveDevice = {}
        )
    }
}
@Preview(
    name = "Connection Timeout Light",
    showBackground = true,
    widthDp = 412,
    heightDp = 900
)
@Composable
private fun ConnectionTimeoutLightPreview() {
    OreoSmartOutletTheme(
        darkTheme = false
    ) {
        DeviceConnectionSetupScreen(
            state = DeviceConnectionSetupUiState(
                ipAddress = "192.168.1.100",
                port = "8080",
                connectionIssue = ConnectionIssueType.TIMEOUT,
                connectionMessage =
                    "The smart outlet did not respond before the request timed out."
            ),
            onIpAddressChange = {},
            onPortChange = {},
            onTestConnection = {},
            onSaveDevice = {}
        )
    }
}

@Preview(
    name = "Invalid Device Dark",
    showBackground = true,
    backgroundColor = 0xFF0F172A,
    widthDp = 412,
    heightDp = 900
)
@Composable
private fun InvalidDeviceDarkPreview() {
    OreoSmartOutletTheme(
        darkTheme = true
    ) {
        DeviceConnectionSetupScreen(
            state = DeviceConnectionSetupUiState(
                ipAddress = "192.168.1.50",
                port = "8080",
                connectionIssue =
                    ConnectionIssueType.INVALID_DEVICE,
                connectionMessage =
                    "The address responded as UnknownDevice, not SmartOutlet_ESP8266."
            ),
            onIpAddressChange = {},
            onPortChange = {},
            onTestConnection = {},
            onSaveDevice = {}
        )
    }
}