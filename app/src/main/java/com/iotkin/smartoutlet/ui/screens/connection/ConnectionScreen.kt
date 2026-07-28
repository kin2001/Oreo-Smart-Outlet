package com.iotkin.smartoutlet.ui.screens.connection

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.iotkin.smartoutlet.data.model.DeviceAddress
import com.iotkin.smartoutlet.discovery.DiscoveredSmartOutlet
import com.iotkin.smartoutlet.ui.components.ConnectionBadge
import com.iotkin.smartoutlet.ui.components.ConnectionStatus
import com.iotkin.smartoutlet.ui.components.OreoCatLogo
import com.iotkin.smartoutlet.ui.components.OreoPrimaryButton
import com.iotkin.smartoutlet.ui.components.OreoSecondaryButton
import com.iotkin.smartoutlet.ui.theme.OreoShapeTokens
import com.iotkin.smartoutlet.ui.theme.OreoSmartOutletTheme
import com.iotkin.smartoutlet.ui.theme.OreoSpacing

@Composable
fun DeviceConnectionSetupScreen(
    state: DeviceConnectionSetupUiState,
    onIpAddressChange: (String) -> Unit,
    onPortChange: (String) -> Unit,
    onRefreshDiscovery: () -> Unit,
    onDiscoveredDeviceSelected: (DiscoveredSmartOutlet) -> Unit,
    onTestConnection: () -> Unit,
    onSaveDevice: () -> Unit,
    onDisconnectDevice: () -> Unit = {},
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isManagingSavedDevice =
        state.savedAddress != null

    var showManualEntry by rememberSaveable(
        isManagingSavedDevice,
        state.automaticDiscoveryEnabled
    ) {
        mutableStateOf(
            isManagingSavedDevice ||
                    !state.automaticDiscoveryEnabled
        )
    }

    var showFindAnotherOutlet by rememberSaveable(
        isManagingSavedDevice
    ) {
        mutableStateOf(false)
    }

    var showForgetConfirmation by rememberSaveable {
        mutableStateOf(false)
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = OreoSpacing.ScreenMargin,
                    vertical = OreoSpacing.StackLarge
                ),
            verticalArrangement = Arrangement.spacedBy(
                OreoSpacing.StackLarge
            )
        ) {
            onBack?.let { backAction ->
                ConnectionBackAction(
                    onClick = backAction
                )
            }

            if (isManagingSavedDevice) {
                ManageSavedDeviceHeader(
                    state = state
                )

                ConnectionAddressForm(
                    state = state,
                    onIpAddressChange = onIpAddressChange,
                    onPortChange = onPortChange
                )

                ConnectionActions(
                    state = state,
                    saveLabel = "Save Changes",
                    onTestConnection =
                        onTestConnection,
                    onSaveDevice = onSaveDevice
                )

                ConnectionActionFeedback(
                    state = state
                )

                ExpandableSectionHeader(
                    title = "Find Another Outlet",
                    expanded = showFindAnotherOutlet,
                    onClick = {
                        showFindAnotherOutlet =
                            !showFindAnotherOutlet

                        if (showFindAnotherOutlet) {
                            onRefreshDiscovery()
                        }
                    }
                )

                AnimatedVisibility(
                    visible = showFindAnotherOutlet,
                    enter =
                        expandVertically(
                            animationSpec = tween(
                                durationMillis = 200
                            )
                        ) +
                                fadeIn(
                                    animationSpec = tween(
                                        durationMillis = 200
                                    )
                                ),
                    exit =
                        shrinkVertically(
                            animationSpec = tween(
                                durationMillis = 200
                            )
                        ) +
                                fadeOut(
                                    animationSpec = tween(
                                        durationMillis = 200
                                    )
                                )
                ) {
                    DiscoveryContent(
                        state = state,
                        onRefreshDiscovery =
                            onRefreshDiscovery,
                        onDiscoveredDeviceSelected = {
                                device ->
                            showFindAnotherOutlet = false
                            onDiscoveredDeviceSelected(
                                device
                            )
                        }
                    )
                }

                ConnectionHelpCard()

                OreoSecondaryButton(
                    text = if (
                        state.isDisconnectingDevice
                    ) {
                        "Forgetting Device"
                    } else {
                        "Forget Device"
                    },
                    onClick = {
                        showForgetConfirmation = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled =
                        !state.isTestingConnection &&
                                !state.isSavingDevice &&
                                !state.isDisconnectingDevice,
                    isLoading =
                        state.isDisconnectingDevice
                )
            } else {
                FirstTimeConnectionHeader()

                DiscoveryContent(
                    state = state,
                    onRefreshDiscovery =
                        onRefreshDiscovery,
                    onDiscoveredDeviceSelected =
                        onDiscoveredDeviceSelected
                )

                ExpandableSectionHeader(
                    title = if (showManualEntry) {
                        "Hide Manual Address"
                    } else {
                        "Enter Address Manually"
                    },
                    expanded = showManualEntry,
                    onClick = {
                        showManualEntry =
                            !showManualEntry
                    }
                )

                AnimatedVisibility(
                    visible = showManualEntry,
                    enter =
                        expandVertically(
                            animationSpec = tween(
                                durationMillis = 200
                            )
                        ) +
                                fadeIn(
                                    animationSpec = tween(
                                        durationMillis = 200
                                    )
                                ),
                    exit =
                        shrinkVertically(
                            animationSpec = tween(
                                durationMillis = 200
                            )
                        ) +
                                fadeOut(
                                    animationSpec = tween(
                                        durationMillis = 200
                                    )
                                )
                ) {
                    ConnectionAddressForm(
                        state = state,
                        onIpAddressChange =
                            onIpAddressChange,
                        onPortChange =
                            onPortChange
                    )
                }

                ConnectionActions(
                    state = state,
                    saveLabel = "Save Device",
                    onTestConnection = {
                        showManualEntry = true
                        onTestConnection()
                    },
                    onSaveDevice = onSaveDevice
                )

                ConnectionActionFeedback(
                    state = state
                )

                ConnectionHelpCard()
            }
        }
    }

    if (showForgetConfirmation) {
        AlertDialog(
            onDismissRequest = {
                if (!state.isDisconnectingDevice) {
                    showForgetConfirmation = false
                }
            },
            title = {
                Text(
                    text = "Forget saved device?"
                )
            },
            text = {
                Text(
                    text =
                        "The saved IP address and port will be removed from this phone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showForgetConfirmation = false
                        onDisconnectDevice()
                    },
                    enabled =
                        !state.isDisconnectingDevice
                ) {
                    Text(
                        text = "Forget"
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showForgetConfirmation = false
                    },
                    enabled =
                        !state.isDisconnectingDevice
                ) {
                    Text(
                        text = "Cancel"
                    )
                }
            }
        )
    }
}

@Composable
private fun ConnectionBackAction(
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick
    ) {
        Icon(
            imageVector =
                Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = null
        )

        Spacer(
            modifier = Modifier.width(
                OreoSpacing.StackSmall
            )
        )

        Text(
            text = "Back"
        )
    }
}

@Composable
private fun FirstTimeConnectionHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
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
            text =
                "Choose a nearby outlet or enter its address manually.",
            style = MaterialTheme.typography.bodyMedium,
            color =
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ManageSavedDeviceHeader(
    state: DeviceConnectionSetupUiState
) {
    val connectionStatus =
        state.savedDeviceConnectionStatus
            ?: SavedDeviceConnectionStatus.SAVED

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(
            OreoSpacing.StackMedium
        )
    ) {
        Text(
            text = "Manage Device",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = state.friendlyDeviceName,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        ConnectionBadge(
            status = connectionStatus.toConnectionStatus()
        )

        Text(
            text = connectionStatus.description(),
            style = MaterialTheme.typography.bodyMedium,
            color =
                MaterialTheme.colorScheme.onSurfaceVariant
        )

        state.savedAddress?.let { savedAddress ->
            Text(
                text =
                    "Saved address: ${savedAddress.displayAddress}",
                style = MaterialTheme.typography.bodyMedium,
                color =
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DiscoveryContent(
    state: DeviceConnectionSetupUiState,
    onRefreshDiscovery: () -> Unit,
    onDiscoveredDeviceSelected:
        (DiscoveredSmartOutlet) -> Unit
) {
    if (state.automaticDiscoveryEnabled) {
        DeviceDiscoverySection(
            discoveryState = state.discovery,
            onRefresh = onRefreshDiscovery,
            onDeviceSelected =
                onDiscoveredDeviceSelected
        )
    } else {
        Text(
            text =
                "Nearby outlet search is off. Use the address fields instead.",
            style = MaterialTheme.typography.bodyMedium,
            color =
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ExpandableSectionHeader(
    title: String,
    expanded: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OreoShapeTokens.Large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = OreoSpacing.StackMedium,
                vertical = OreoSpacing.StackSmall
            ),
            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Icon(
                imageVector =
                    Icons.Filled.KeyboardArrowDown,
                contentDescription =
                    if (expanded) {
                        "Collapse"
                    } else {
                        "Expand"
                    }
            )
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
        color =
            MaterialTheme.colorScheme
                .surfaceContainerLowest,
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
                text = "Connection Address",
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
                supportingText =
                    state.ipError?.let { error ->
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
                    keyboardType =
                        KeyboardType.Decimal,
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
                supportingText =
                    state.portError?.let { error ->
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
                    keyboardType =
                        KeyboardType.Number,
                    imeAction = ImeAction.Done
                )
            )
        }
    }
}

@Composable
private fun ConnectionActions(
    state: DeviceConnectionSetupUiState,
    saveLabel: String,
    onTestConnection: () -> Unit,
    onSaveDevice: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
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
            enabled =
                !state.isTestingConnection &&
                        !state.isSavingDevice,
            isLoading = state.isTestingConnection
        )

        OreoPrimaryButton(
            text = if (state.isSavingDevice) {
                "Saving"
            } else {
                saveLabel
            },
            onClick = onSaveDevice,
            modifier = Modifier.fillMaxWidth(),
            enabled =
                state.isDeviceVerified &&
                        !state.isTestingConnection &&
                        !state.isSavingDevice,
            isLoading = state.isSavingDevice
        )
    }
}

@Composable
private fun ConnectionActionFeedback(
    state: DeviceConnectionSetupUiState
) {
    when {
        state.connectionIssue != null -> {
            InlineConnectionFeedback(
                title = when (
                    state.connectionIssue
                ) {
                    ConnectionIssueType.TIMEOUT ->
                        "Connection timed out"

                    ConnectionIssueType.INVALID_DEVICE ->
                        "Incompatible device"

                    ConnectionIssueType.CONNECTION_FAILED ->
                        "Connection failed"
                },
                message = state.connectionMessage
                    ?: "Unable to connect to the smart outlet.",
                isError = true
            )
        }

        state.isDeviceVerified &&
                state.connectionMessage != null -> {
            InlineConnectionFeedback(
                title = "Connection verified",
                message = state.connectionMessage,
                isError = false
            )
        }
    }

    state.saveError?.let { message ->
        InlineConnectionFeedback(
            title = "Unable to save device",
            message = message,
            isError = true
        )
    }
}

@Composable
private fun InlineConnectionFeedback(
    title: String,
    message: String,
    isError: Boolean
) {
    val accentColor = if (isError) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = OreoSpacing.StackSmall
            ),
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

@Composable
private fun ConnectionHelpCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OreoShapeTokens.Large,
        color =
            MaterialTheme.colorScheme.surfaceContainerLow
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
                text =
                    "Your phone and smart outlet must be connected to the same Wi-Fi network.",
                style = MaterialTheme.typography.bodyMedium,
                color =
                    MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "The default API port is 8080.",
                style = MaterialTheme.typography.bodyMedium,
                color =
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun SavedDeviceConnectionStatus
        .toConnectionStatus(): ConnectionStatus {
    return when (this) {
        SavedDeviceConnectionStatus.SAVED ->
            ConnectionStatus.Saved

        SavedDeviceConnectionStatus.ONLINE ->
            ConnectionStatus.Online

        SavedDeviceConnectionStatus.RECONNECTING ->
            ConnectionStatus.Reconnecting

        SavedDeviceConnectionStatus.STALE ->
            ConnectionStatus.Stale

        SavedDeviceConnectionStatus.OFFLINE ->
            ConnectionStatus.Offline
    }
}

private fun SavedDeviceConnectionStatus
        .description(): String {
    return when (this) {
        SavedDeviceConnectionStatus.SAVED ->
            "Saved on this phone. Test the connection to verify it."

        SavedDeviceConnectionStatus.ONLINE ->
            "Verified by a successful live response."

        SavedDeviceConnectionStatus.RECONNECTING ->
            "Checking the saved outlet now."

        SavedDeviceConnectionStatus.STALE ->
            "Previously verified, but the latest check failed."

        SavedDeviceConnectionStatus.OFFLINE ->
            "Saved on this phone, but it did not respond."
    }
}

@Composable
fun DeviceConnectionSetupRoute(
    onDeviceSaved: () -> Unit,
    onDeviceDisconnected: () -> Unit = {},
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    setupViewModel:
        DeviceConnectionSetupViewModel = viewModel()
) {
    val state by
        setupViewModel.uiState
            .collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(
        lifecycleOwner,
        setupViewModel
    ) {
        val observer =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_START ->
                        setupViewModel
                            .startDiscovery()

                    Lifecycle.Event.ON_STOP ->
                        setupViewModel
                            .stopDiscovery()

                    else -> Unit
                }
            }

        lifecycleOwner.lifecycle
            .addObserver(observer)

        if (
            lifecycleOwner.lifecycle.currentState
                .isAtLeast(
                    Lifecycle.State.STARTED
                )
        ) {
            setupViewModel.startDiscovery()
        }

        onDispose {
            lifecycleOwner.lifecycle
                .removeObserver(observer)
            setupViewModel.stopDiscovery()
        }
    }

    LaunchedEffect(setupViewModel) {
        setupViewModel.events.collect { event ->
            when (event) {
                DeviceConnectionSetupEvent.DeviceSaved ->
                    onDeviceSaved()

                DeviceConnectionSetupEvent
                    .DeviceDisconnected ->
                    onDeviceDisconnected()
            }
        }
    }

    DeviceConnectionSetupScreen(
        state = state,
        onIpAddressChange =
            setupViewModel::onIpAddressChange,
        onPortChange =
            setupViewModel::onPortChange,
        onRefreshDiscovery =
            setupViewModel::refreshDiscovery,
        onDiscoveredDeviceSelected =
            setupViewModel::selectDiscoveredDevice,
        onTestConnection =
            setupViewModel::testConnection,
        onSaveDevice =
            setupViewModel::saveDevice,
        onDisconnectDevice =
            setupViewModel::disconnectDevice,
        onBack = onBack,
        modifier = modifier
    )
}

@Preview(
    name = "First-time Connection Light",
    showBackground = true,
    widthDp = 360,
    heightDp = 900
)
@Composable
private fun FirstTimeConnectionPreview() {
    OreoSmartOutletTheme(
        darkTheme = false
    ) {
        DeviceConnectionSetupScreen(
            state =
                DeviceConnectionSetupUiState(),
            onIpAddressChange = {},
            onPortChange = {},
            onRefreshDiscovery = {},
            onDiscoveredDeviceSelected = {},
            onTestConnection = {},
            onSaveDevice = {}
        )
    }
}

@Preview(
    name = "Saved Device Dark",
    showBackground = true,
    backgroundColor = 0xFF0F172A,
    widthDp = 360,
    heightDp = 900
)
@Composable
private fun SavedDevicePreview() {
    OreoSmartOutletTheme(
        darkTheme = true
    ) {
        DeviceConnectionSetupScreen(
            state =
                DeviceConnectionSetupUiState(
                    savedAddress =
                        DeviceAddress(
                            host = "192.168.8.113",
                            port = 8080
                        ),
                    friendlyDeviceName =
                        "Oreo Smart Outlet",
                    ipAddress = "192.168.8.113",
                    port = "8080"
                ),
            onIpAddressChange = {},
            onPortChange = {},
            onRefreshDiscovery = {},
            onDiscoveredDeviceSelected = {},
            onTestConnection = {},
            onSaveDevice = {},
            onBack = {}
        )
    }
}

@Preview(
    name = "Verified Device Large Font",
    showBackground = true,
    widthDp = 320,
    heightDp = 900,
    fontScale = 1.5f
)
@Composable
private fun VerifiedDeviceLargeFontPreview() {
    OreoSmartOutletTheme(
        darkTheme = false
    ) {
        DeviceConnectionSetupScreen(
            state =
                DeviceConnectionSetupUiState(
                    savedAddress =
                        DeviceAddress(
                            host = "192.168.8.113",
                            port = 8080
                        ),
                    ipAddress = "192.168.8.113",
                    port = "8080",
                    isDeviceVerified = true,
                    hasVerifiedLiveResponse = true,
                    connectionMessage =
                        "Connected to Oreo Smart Outlet."
                ),
            onIpAddressChange = {},
            onPortChange = {},
            onRefreshDiscovery = {},
            onDiscoveredDeviceSelected = {},
            onTestConnection = {},
            onSaveDevice = {},
            onBack = {}
        )
    }
}
