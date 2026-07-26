package com.iotkin.smartoutlet.ui.screens.diagnostics

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iotkin.smartoutlet.data.model.DeviceStatusResponse
import com.iotkin.smartoutlet.data.repository.DeviceConnectionState
import com.iotkin.smartoutlet.data.repository.DeviceStatusRepositoryState
import com.iotkin.smartoutlet.ui.theme.OreoShapeTokens
import com.iotkin.smartoutlet.ui.theme.OreoSmartOutletTheme
import com.iotkin.smartoutlet.ui.theme.OreoSpacing
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun DiagnosticsRoute(
    viewModel: DiagnosticsViewModel,
    onManageDevice: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState
        .collectAsStateWithLifecycle()

    val appSettings by viewModel.appSettings
        .collectAsStateWithLifecycle()

    val context = LocalContext.current

    DiagnosticsScreen(
        state = state,
        outlet1Name =
            appSettings.outlet1FriendlyName,
        outlet2Name =
            appSettings.outlet2FriendlyName,
        onRefreshDeviceStatus =
            viewModel::refreshDeviceStatus,
        onRequestTimeSync =
            viewModel::requestTimeSync,
        onManageDevice = onManageDevice,
        onCopyDeviceAddress = {
            val address =
                state.repositoryState
                    .address
                    ?.displayAddress

            if (address != null) {
                copyDeviceAddress(
                    context = context,
                    address = address
                )
            }
        },
        onDismissFeedback =
            viewModel::clearActionFeedback,
        modifier = modifier
    )
}

@Composable
fun DiagnosticsScreen(
    state: DiagnosticsUiState,
    outlet1Name: String,
    outlet2Name: String,
    onRefreshDeviceStatus: () -> Unit,
    onRequestTimeSync: () -> Unit,
    onManageDevice: () -> Unit,
    onCopyDeviceAddress: () -> Unit,
    onDismissFeedback: () -> Unit,
    modifier: Modifier = Modifier
) {
    val repositoryState =
        state.repositoryState

    var localFeedbackMessage by remember {
        mutableStateOf<String?>(null)
    }

    var localFeedbackIsError by remember {
        mutableStateOf(false)
    }

    val repositoryFeedbackMessage =
        state.actionError
            ?: state.actionMessage

    val feedbackMessage =
        repositoryFeedbackMessage
            ?: localFeedbackMessage

    val feedbackIsError =
        when {
            state.actionError != null -> true
            state.actionMessage != null -> false
            else -> localFeedbackIsError
        }

    LaunchedEffect(feedbackMessage) {
        if (feedbackMessage == null) {
            return@LaunchedEffect
        }

        delay(FEEDBACK_DURATION_MILLISECONDS)

        if (repositoryFeedbackMessage != null) {
            onDismissFeedback()
        }

        localFeedbackMessage = null
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(
                horizontal =
                    OreoSpacing.ScreenMargin
            ),
        contentPadding = PaddingValues(
            bottom = OreoSpacing.StackMedium
        ),
        verticalArrangement =
            Arrangement.spacedBy(
                OreoSpacing.StackMedium
            )
    ) {
        item {
            DiagnosticsHeader(
                modifier = Modifier.padding(
                    top = OreoSpacing.StackMedium
                )
            )
        }

        item {
            DeviceHealthSummaryCard(
                state = repositoryState,
                onCopyDeviceAddress = {
                    onCopyDeviceAddress()

                    localFeedbackIsError = false
                    localFeedbackMessage =
                        "Device address copied."
                }
            )
        }

        item {
            DiagnosticsActionsCard(
                state = state,
                feedbackMessage = feedbackMessage,
                feedbackIsError = feedbackIsError,
                onRefreshDeviceStatus =
                    onRefreshDeviceStatus,
                onRequestTimeSync =
                    onRequestTimeSync,
                onManageDevice = onManageDevice
            )
        }

        if (repositoryState.isInitialLoading) {
            item {
                DiagnosticsLoadingCard()
            }
        }

        repositoryState.status?.let { status ->
            item {
                TechnicalDetailsSection(
                    state = repositoryState,
                    status = status,
                    outlet1Name = outlet1Name,
                    outlet2Name = outlet2Name
                )
            }
        }

        item {
            Text(
                text =
                    "Diagnostics only displays information returned by the device or generated by this app.",
                style =
                    MaterialTheme.typography.bodySmall,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant,
                modifier = Modifier.padding(
                    bottom =
                        OreoSpacing.StackMedium
                )
            )
        }
    }
}

@Composable
private fun DiagnosticsHeader(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement =
            Arrangement.spacedBy(
                OreoSpacing.StackSmall
            )
    ) {
        Text(
            text = "Diagnostics",
            style =
                MaterialTheme.typography
                    .headlineLarge,
            color =
                MaterialTheme.colorScheme
                    .onBackground
        )

        Text(
            text =
                "See your outlet's health, connection, and device information.",
            style =
                MaterialTheme.typography
                    .bodyLarge,
            color =
                MaterialTheme.colorScheme
                    .onSurfaceVariant
        )
    }
}

private data class HealthPresentation(
    val title: String,
    val description: String,
    val connectionText: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
private fun DeviceHealthSummaryCard(
    state: DeviceStatusRepositoryState,
    onCopyDeviceAddress: () -> Unit
) {
    val status = state.status

    val wifiQuality =
        status?.let { currentStatus ->
            wifiQualityLabel(currentStatus.rssi)
        }

    val presentation =
        when (state.connectionState) {
            DeviceConnectionState.NO_SAVED_DEVICE ->
                HealthPresentation(
                    title = "No device is connected",
                    description =
                        "Add or select your smart outlet to see its status.",
                    connectionText =
                        "No saved device",
                    icon = Icons.Filled.Info,
                    color =
                        MaterialTheme.colorScheme
                            .error
                )

            DeviceConnectionState.CONNECTING ->
                HealthPresentation(
                    title = "Connecting to your outlet",
                    description =
                        "Keep your phone on the same Wi-Fi network while the app connects.",
                    connectionText = "Connecting",
                    icon = Icons.Filled.Refresh,
                    color =
                        MaterialTheme.colorScheme
                            .tertiary
                )

            DeviceConnectionState.RECONNECTING ->
                HealthPresentation(
                    title = "Checking your connection",
                    description =
                        if (state.isStale) {
                            "A status refresh was delayed. Last confirmed data remains visible while the app tries again."
                        } else {
                            "Keep your phone and outlet on the same Wi-Fi network."
                        },
                    connectionText = "Reconnecting",
                    icon = Icons.Filled.Refresh,
                    color =
                        MaterialTheme.colorScheme
                            .primary
                )

            DeviceConnectionState.OFFLINE ->
                HealthPresentation(
                    title = "Your outlet is offline",
                    description =
                        "Check that the outlet has power and that your phone uses the same Wi-Fi network. Then refresh the status.",
                    connectionText = "Offline",
                    icon = Icons.Filled.Warning,
                    color =
                        MaterialTheme.colorScheme
                            .error
                )

            DeviceConnectionState.ONLINE ->
                when {
                    status == null ->
                        HealthPresentation(
                            title =
                                "Connected, checking status",
                            description =
                                "The app is waiting for the latest device information.",
                            connectionText = "Connected",
                            icon =
                                Icons.Filled.CheckCircle,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .primary
                        )

                    !status.timeValid ->
                        HealthPresentation(
                            title =
                                "Device clock needs attention",
                            description =
                                "Your outlet is connected. Sync its clock so schedules use the correct time.",
                            connectionText = "Connected",
                            icon = Icons.Filled.Warning,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .tertiary
                        )

                    wifiQuality == "Fair" ||
                            wifiQuality == "Weak" ->
                        HealthPresentation(
                            title =
                                "Wi-Fi signal could be better",
                            description =
                                if (wifiQuality == "Weak") {
                                    "Your outlet is connected, but its Wi-Fi signal is weak. Moving it or the router closer may improve reliability."
                                } else {
                                    "Your outlet is connected with a fair Wi-Fi signal. Moving it or the router closer may improve reliability."
                                },
                            connectionText = "Connected",
                            icon = Icons.Filled.Warning,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .tertiary
                        )

                    else ->
                        HealthPresentation(
                            title =
                                "Everything looks good",
                            description =
                                "Your outlet is connected, its Wi-Fi signal is ${wifiQuality?.lowercase() ?: "available"}, and its clock is ready.",
                            connectionText = "Connected",
                            icon =
                                Icons.Filled.CheckCircle,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .primary
                        )
                }
        }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OreoShapeTokens.ExtraLarge,
        color =
            presentation.color.copy(
                alpha = 0.08f
            ),
        border = BorderStroke(
            width = 1.dp,
            color =
                presentation.color.copy(
                    alpha = 0.30f
                )
        )
    ) {
        Column(
            modifier =
                Modifier.padding(
                    OreoSpacing.CardPadding
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    OreoSpacing.StackMedium
                )
        ) {
            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(
                        OreoSpacing.StackMedium
                    ),
                verticalAlignment =
                    Alignment.Top
            ) {
                Icon(
                    imageVector =
                        presentation.icon,
                    contentDescription = null,
                    tint = presentation.color,
                    modifier =
                        Modifier.size(
                            OreoSpacing.StandardIcon
                        )
                )

                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(
                            OreoSpacing.StackSmall
                        )
                ) {
                    Text(
                        text = presentation.title,
                        style =
                            MaterialTheme.typography
                                .headlineMedium,
                        color =
                            MaterialTheme.colorScheme
                                .onSurface
                    )

                    Text(
                        text =
                            presentation.description,
                        style =
                            MaterialTheme.typography
                                .bodyMedium,
                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )
                }
            }

            HealthStatusRow(
                icon = presentation.icon,
                text =
                    "Connection: ${presentation.connectionText}",
                color = presentation.color
            )

            if (state.isStale) {
                HealthStatusRow(
                    icon = Icons.Filled.Warning,
                    text =
                        "Status: Showing last confirmed data",
                    color = presentation.color
                )
            }

            status?.let { currentStatus ->
                val signalQuality =
                    wifiQualityLabel(
                        currentStatus.rssi
                    )

                val signalNeedsAttention =
                    signalQuality == "Fair" ||
                            signalQuality == "Weak"

                HealthStatusRow(
                    icon =
                        if (signalNeedsAttention) {
                            Icons.Filled.Warning
                        } else {
                            Icons.Filled.CheckCircle
                        },
                    text =
                        "Wi-Fi signal: $signalQuality",
                    color =
                        if (signalNeedsAttention) {
                            MaterialTheme.colorScheme
                                .tertiary
                        } else {
                            MaterialTheme.colorScheme
                                .primary
                        }
                )

                HealthStatusRow(
                    icon =
                        if (currentStatus.timeValid) {
                            Icons.Filled.CheckCircle
                        } else {
                            Icons.Filled.Warning
                        },
                    text =
                        if (currentStatus.timeValid) {
                            "Device clock: Ready"
                        } else {
                            "Device clock: Needs synchronization"
                        },
                    color =
                        if (currentStatus.timeValid) {
                            MaterialTheme.colorScheme
                                .primary
                        } else {
                            MaterialTheme.colorScheme
                                .tertiary
                        }
                )
            }

            state.address?.let { address ->
                HorizontalDivider(
                    color =
                        MaterialTheme.colorScheme
                            .outlineVariant
                            .copy(alpha = 0.35f)
                )

                DeviceAddressRow(
                    address = address.displayAddress,
                    onCopy = onCopyDeviceAddress
                )
            }
        }
    }
}

@Composable
private fun HealthStatusRow(
    icon: ImageVector,
    text: String,
    color: Color
) {
    Row(
        horizontalArrangement =
            Arrangement.spacedBy(
                OreoSpacing.StackSmall
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier =
                Modifier.size(
                    OreoSpacing.StandardIcon
                )
        )

        Text(
            text = text,
            style =
                MaterialTheme.typography
                    .bodyMedium,
            color =
                MaterialTheme.colorScheme
                    .onSurface
        )
    }
}

@Composable
private fun DeviceAddressRow(
    address: String,
    onCopy: () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth()
    ) {
        val useStackedLayout =
            maxWidth /
                    LocalDensity.current.fontScale <
                    300.dp

        if (useStackedLayout) {
            Column {
                Text(
                    text = "Device address",
                    style =
                        MaterialTheme.typography
                            .bodySmall,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )

                Text(
                    text = address,
                    style =
                        MaterialTheme.typography
                            .bodyMedium,
                    fontWeight =
                        FontWeight.SemiBold
                )

                TextButton(
                    onClick = onCopy,
                    modifier =
                        Modifier.align(
                            Alignment.End
                        )
                ) {
                    Text("Copy")
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(
                        OreoSpacing.StackSmall
                    ),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Device address",
                        style =
                            MaterialTheme.typography
                                .bodySmall,
                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )

                    Text(
                        text = address,
                        style =
                            MaterialTheme.typography
                                .bodyMedium,
                        fontWeight =
                            FontWeight.SemiBold
                    )
                }

                TextButton(
                    onClick = onCopy
                ) {
                    Text("Copy")
                }
            }
        }
    }
}

@Composable
private fun ConnectionSummaryCard(
    state: DeviceStatusRepositoryState
) {
    val statusText =
        when (state.connectionState) {
            DeviceConnectionState.NO_SAVED_DEVICE ->
                "No saved device"

            DeviceConnectionState.CONNECTING ->
                "Connecting"

            DeviceConnectionState.ONLINE ->
                "Online"

            DeviceConnectionState.RECONNECTING ->
                "Reconnecting"

            DeviceConnectionState.OFFLINE ->
                "Offline"
        }

    val statusColor =
        when (state.connectionState) {
            DeviceConnectionState.ONLINE ->
                MaterialTheme.colorScheme.primary

            DeviceConnectionState.CONNECTING,
            DeviceConnectionState.RECONNECTING ->
                MaterialTheme.colorScheme.tertiary

            DeviceConnectionState.NO_SAVED_DEVICE,
            DeviceConnectionState.OFFLINE ->
                MaterialTheme.colorScheme.error
        }

    DiagnosticsCard(
        title = "Connection"
    ) {
        DiagnosticValueRow(
            label = "State",
            value = statusText,
            valueColor = statusColor
        )

        DiagnosticValueRow(
            label = "Device address",
            value =
                state.address
                    ?.displayAddress
                    ?: "Not saved"
        )

        DiagnosticValueRow(
            label = "Last refresh",
            value =
                formatRefreshTimestamp(
                    state
                        .lastSuccessfulRefreshEpochMillis
                )
        )

        if (state.isStale) {
            HorizontalDivider(
                color =
                    MaterialTheme.colorScheme
                        .error
                        .copy(alpha = 0.20f)
            )

            InlineStatusMessage(
                message =
                    state.errorMessage
                        ?.let { error ->
                            "$error Showing the last confirmed values."
                        }
                        ?: "Connection lost. Showing the last confirmed values.",
                isError = true
            )
        }
    }
}

@Composable
private fun DiagnosticsActionsCard(
    state: DiagnosticsUiState,
    feedbackMessage: String?,
    feedbackIsError: Boolean,
    onRefreshDeviceStatus: () -> Unit,
    onRequestTimeSync: () -> Unit,
    onManageDevice: () -> Unit
) {
    val repositoryState =
        state.repositoryState

    val statusActionRunning =
        state.isRunningStatusAction

    val anyActionRunning =
        statusActionRunning ||
                state.isRequestingTimeSync

    DiagnosticsCard(
        title = "Actions"
    ) {
        Text(
            text =
                "These actions do not change relay or schedule settings.",
            style =
                MaterialTheme.typography.bodySmall,
            color =
                MaterialTheme.colorScheme
                    .onSurfaceVariant
        )

        Button(
            onClick = onRefreshDeviceStatus,
            enabled = !anyActionRunning,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = null
            )

            Text(
                text =
                    if (statusActionRunning) {
                        "Refreshing Status..."
                    } else {
                        "Refresh Status"
                    },
                modifier =
                    Modifier.padding(
                        start =
                            OreoSpacing.StackSmall
                    )
            )
        }

        HorizontalDivider(
            color =
                MaterialTheme.colorScheme
                    .outlineVariant
                    .copy(alpha = 0.35f)
        )

        DiagnosticsActionRow(
            label =
                if (
                    state.isRequestingTimeSync
                ) {
                    "Syncing..."
                } else {
                    "Sync Device Clock"
                },
            icon = Icons.Filled.Refresh,
            onClick = onRequestTimeSync,
            enabled =
                !anyActionRunning &&
                        repositoryState.address != null
        )

        HorizontalDivider(
            color =
                MaterialTheme.colorScheme
                    .outlineVariant
                    .copy(alpha = 0.35f)
        )

        DiagnosticsActionRow(
            label = "Manage Device",
            icon = Icons.Filled.Settings,
            trailingIcon =
                Icons.Filled
                    .KeyboardArrowRight,
            onClick = onManageDevice,
            enabled = !anyActionRunning
        )

        feedbackMessage?.let { message ->
            HorizontalDivider(
                color =
                    if (feedbackIsError) {
                        MaterialTheme.colorScheme
                            .error
                            .copy(alpha = 0.20f)
                    } else {
                        MaterialTheme.colorScheme
                            .primary
                            .copy(alpha = 0.20f)
                    }
            )

            InlineStatusMessage(
                message = message,
                isError = feedbackIsError
            )
        }
    }
}

@Composable
private fun DiagnosticsActionRow(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean,
    trailingIcon: ImageVector? = null
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        contentPadding =
            PaddingValues(
                horizontal = 0.dp,
                vertical =
                    OreoSpacing.StackSmall
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier =
                Modifier.size(
                    OreoSpacing.StandardIcon
                )
        )

        Text(
            text = label,
            modifier =
                Modifier
                    .weight(1f)
                    .padding(
                        horizontal =
                            OreoSpacing.StackMedium
                    ),
            textAlign = TextAlign.Start
        )

        trailingIcon?.let { currentIcon ->
            Icon(
                imageVector = currentIcon,
                contentDescription = null,
                modifier =
                    Modifier.size(
                        OreoSpacing.StandardIcon
                    )
            )
        }
    }
}

@Composable
private fun InlineStatusMessage(
    message: String,
    isError: Boolean
) {
    val messageColor =
        if (isError) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.primary
        }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
        Row(
            horizontalArrangement =
                Arrangement.spacedBy(
                    OreoSpacing.StackSmall
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                text = message,
                style =
                    MaterialTheme.typography
                        .bodyMedium,
                color = messageColor,
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
private fun TechnicalDetailsSection(
    state: DeviceStatusRepositoryState,
    status: DeviceStatusResponse,
    outlet1Name: String,
    outlet2Name: String
) {
    var expanded by rememberSaveable {
        mutableStateOf(false)
    }

    Column(
        verticalArrangement =
            Arrangement.spacedBy(
                OreoSpacing.StackMedium
            )
    ) {
        OutlinedButton(
            onClick = {
                expanded = !expanded
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Technical Details",
                textAlign = TextAlign.Start,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector =
                    if (expanded) {
                        Icons.Filled
                            .KeyboardArrowUp
                    } else {
                        Icons.Filled
                            .KeyboardArrowDown
                    },
                contentDescription =
                    if (expanded) {
                        "Collapse technical details"
                    } else {
                        "Expand technical details"
                    }
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter =
                expandVertically() +
                        fadeIn(),
            exit =
                shrinkVertically() +
                        fadeOut()
        ) {
            Column(
                verticalArrangement =
                    Arrangement.spacedBy(
                        OreoSpacing.StackMedium
                    )
            ) {
                ConnectionSummaryCard(
                    state = state
                )

                NetworkInformationCard(
                    status = status
                )

                TimeInformationCard(
                    status = status
                )

                RelayInformationCard(
                    status = status,
                    outlet1Name = outlet1Name,
                    outlet2Name = outlet2Name
                )

                DeviceInformationCard(
                    status = status
                )

                SystemInformationCard(
                    status = status
                )

                ReadinessInformationCard(
                    status = status
                )
            }
        }
    }
}

@Composable
private fun NetworkInformationCard(
    status: DeviceStatusResponse
) {
    DiagnosticsCard(
        title = "Network"
    ) {
        DiagnosticValueRow(
            label = "Device online",
            value = yesNo(status.online)
        )

        DiagnosticValueRow(
            label = "IP address",
            value = status.ip
        )

        DiagnosticValueRow(
            label = "API port",
            value = status.apiPort.toString()
        )

        DiagnosticValueRow(
            label = "Wi-Fi network",
            value =
                status.ssid.ifBlank {
                    "Not reported"
                }
        )

        DiagnosticValueRow(
            label = "Signal strength",
            value = "${status.rssi} dBm"
        )
    }
}

@Composable
private fun TimeInformationCard(
    status: DeviceStatusResponse
) {
    DiagnosticsCard(
        title = "Time"
    ) {
        DiagnosticValueRow(
            label = "Time valid",
            value = yesNo(status.timeValid)
        )

        DiagnosticValueRow(
            label = "Philippine time",
            value =
                status.philippineTime.ifBlank {
                    "Not reported"
                }
        )
    }
}

@Composable
private fun RelayInformationCard(
    status: DeviceStatusResponse,
    outlet1Name: String,
    outlet2Name: String
) {
    DiagnosticsCard(
        title = "Relays and schedules"
    ) {
        Text(
            text = "OUTLET 1 - $outlet1Name",
            style =
                MaterialTheme.typography
                    .titleMedium,
            color =
                MaterialTheme.colorScheme
                    .onSurface
        )

        DiagnosticValueRow(
            label = "State",
            value =
                relayState(
                    status.relay1.state
                )
        )

        DiagnosticValueRow(
            label = "Schedule enabled",
            value =
                yesNo(
                    status.relay1
                        .schedule
                        .enabled
                )
        )

        DiagnosticValueRow(
            label = "Scheduled on",
            value =
                formatScheduleTime(
                    hour =
                        status.relay1
                            .schedule
                            .onHour,
                    minute =
                        status.relay1
                            .schedule
                            .onMinute
                )
        )

        DiagnosticValueRow(
            label = "Scheduled off",
            value =
                formatScheduleTime(
                    hour =
                        status.relay1
                            .schedule
                            .offHour,
                    minute =
                        status.relay1
                            .schedule
                            .offMinute
                )
        )

        HorizontalDivider()

        Text(
            text = "OUTLET 2 - $outlet2Name",
            style =
                MaterialTheme.typography
                    .titleMedium,
            color =
                MaterialTheme.colorScheme
                    .onSurface
        )

        DiagnosticValueRow(
            label = "State",
            value =
                relayState(
                    status.relay2.state
                )
        )

        DiagnosticValueRow(
            label = "Schedule enabled",
            value =
                yesNo(
                    status.relay2
                        .schedule
                        .enabled
                )
        )

        DiagnosticValueRow(
            label = "Scheduled on",
            value =
                formatScheduleTime(
                    hour =
                        status.relay2
                            .schedule
                            .onHour,
                    minute =
                        status.relay2
                            .schedule
                            .onMinute
                )
        )

        DiagnosticValueRow(
            label = "Scheduled off",
            value =
                formatScheduleTime(
                    hour =
                        status.relay2
                            .schedule
                            .offHour,
                    minute =
                        status.relay2
                            .schedule
                            .offMinute
                )
        )
    }
}

@Composable
private fun DeviceInformationCard(
    status: DeviceStatusResponse
) {
    DiagnosticsCard(
        title = "Device and firmware"
    ) {
        DiagnosticValueRow(
            label = "Device type",
            value = status.device
        )

        DiagnosticValueRow(
            label = "Device ID",
            value = status.deviceId
        )

        DiagnosticValueRow(
            label = "Firmware version",
            value = status.firmwareVersion
        )

        DiagnosticValueRow(
            label = "API version",
            value =
                status.apiVersion.toString()
        )
    }
}

@Composable
private fun SystemInformationCard(
    status: DeviceStatusResponse
) {
    DiagnosticsCard(
        title = "System"
    ) {
        DiagnosticValueRow(
            label = "Uptime",
            value =
                formatUptime(
                    status.uptimeSeconds
                )
        )

        DiagnosticValueRow(
            label = "Free heap",
            value =
                formatBytes(
                    status.freeHeapBytes
                )
        )

        DiagnosticValueRow(
            label = "Free sketch space",
            value =
                formatBytes(
                    status.freeSketchSpaceBytes
                )
        )

        DiagnosticValueRow(
            label = "Reset reason",
            value =
                status.resetReason.ifBlank {
                    "Not reported"
                }
        )
    }
}

@Composable
private fun ReadinessInformationCard(
    status: DeviceStatusResponse
) {
    DiagnosticsCard(
        title = "Readiness"
    ) {
        DiagnosticValueRow(
            label = "OTA ready",
            value = yesNo(status.otaReady)
        )

        DiagnosticValueRow(
            label = "API active",
            value = yesNo(status.apiActive)
        )
    }
}

@Composable
private fun DiagnosticsLoadingCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OreoShapeTokens.ExtraLarge,
        color =
            MaterialTheme.colorScheme
                .surfaceContainerLowest
    ) {
        Row(
            modifier = Modifier.padding(
                OreoSpacing.CardPadding
            ),
            horizontalArrangement =
                Arrangement.spacedBy(
                    OreoSpacing.StackMedium
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            CircularProgressIndicator()

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(
                        OreoSpacing.StackSmall
                    )
            ) {
                Text(
                    text =
                        "Loading device status",
                    style =
                        MaterialTheme.typography
                            .titleMedium
                )

                Text(
                    text =
                        "Waiting for the ESP8266 status response.",
                    style =
                        MaterialTheme.typography
                            .bodyMedium,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DiagnosticsCard(
    title: String,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OreoShapeTokens.ExtraLarge,
        color =
            MaterialTheme.colorScheme
                .surfaceContainerLowest,
        border = BorderStroke(
            width = 1.dp,
            color =
                MaterialTheme.colorScheme
                    .outlineVariant
                    .copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier.padding(
                OreoSpacing.CardPadding
            ),
            verticalArrangement =
                Arrangement.spacedBy(
                    OreoSpacing.StackMedium
                )
        ) {
            Text(
                text = title,
                style =
                    MaterialTheme.typography
                        .headlineMedium,
                color =
                    MaterialTheme.colorScheme
                        .onSurface
            )

            content()
        }
    }
}

@Composable
private fun DiagnosticValueRow(
    label: String,
    value: String,
    valueColor:
    androidx.compose.ui.graphics.Color =
        MaterialTheme.colorScheme.onSurface
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
    ) {
        val useStackedLayout =
            maxWidth /
                    LocalDensity.current.fontScale <
                    300.dp

        if (useStackedLayout) {
            Column(
                verticalArrangement =
                    Arrangement.spacedBy(
                        OreoSpacing.Base
                    )
            ) {
                Text(
                    text = label,
                    style =
                        MaterialTheme.typography
                            .bodyMedium,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )

                Text(
                    text = value,
                    style =
                        MaterialTheme.typography
                            .bodyMedium,
                    fontWeight =
                        FontWeight.SemiBold,
                    color = valueColor
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(
                        OreoSpacing.StackMedium
                    ),
                verticalAlignment =
                    Alignment.Top
            ) {
                Text(
                    text = label,
                    style =
                        MaterialTheme.typography
                            .bodyMedium,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant,
                    modifier =
                        Modifier.weight(1f)
                )

                Text(
                    text = value,
                    style =
                        MaterialTheme.typography
                            .bodyMedium,
                    fontWeight =
                        FontWeight.SemiBold,
                    color = valueColor,
                    textAlign = TextAlign.End,
                    modifier =
                        Modifier.weight(1f)
                )
            }
        }
    }
}

internal fun wifiQualityLabel(
    rssi: Int
): String {
    return when {
        rssi >= -50 -> "Strong"
        rssi >= -60 -> "Good"
        rssi >= -70 -> "Fair"
        else -> "Weak"
    }
}

private fun yesNo(
    value: Boolean
): String {
    return if (value) {
        "Yes"
    } else {
        "No"
    }
}

private fun relayState(
    value: Boolean
): String {
    return if (value) {
        "On"
    } else {
        "Off"
    }
}

private fun formatScheduleTime(
    hour: Int,
    minute: Int
): String {
    return String.format(
        Locale.US,
        "%02d:%02d",
        hour,
        minute
    )
}

private fun formatUptime(
    totalSeconds: Long
): String {
    val days =
        totalSeconds / 86_400

    val hours =
        totalSeconds % 86_400 / 3_600

    val minutes =
        totalSeconds % 3_600 / 60

    val seconds =
        totalSeconds % 60

    return when {
        days > 0 -> {
            "${days}d ${hours}h ${minutes}m"
        }

        hours > 0 -> {
            "${hours}h ${minutes}m ${seconds}s"
        }

        minutes > 0 -> {
            "${minutes}m ${seconds}s"
        }

        else -> {
            "${seconds}s"
        }
    }
}

private fun formatBytes(
    bytes: Long
): String {
    val kilobytes =
        bytes / 1_024.0

    val megabytes =
        kilobytes / 1_024.0

    return when {
        megabytes >= 1.0 -> {
            String.format(
                Locale.US,
                "%.2f MB (%d bytes)",
                megabytes,
                bytes
            )
        }

        kilobytes >= 1.0 -> {
            String.format(
                Locale.US,
                "%.1f KB (%d bytes)",
                kilobytes,
                bytes
            )
        }

        else -> {
            "$bytes bytes"
        }
    }
}

private fun formatRefreshTimestamp(
    epochMillis: Long?
): String {
    if (epochMillis == null) {
        return "Never"
    }

    val formatter =
        DateTimeFormatter.ofPattern(
            "MMM d, yyyy h:mm:ss a",
            Locale.US
        )

    return Instant
        .ofEpochMilli(epochMillis)
        .atZone(
            ZoneId.systemDefault()
        )
        .format(formatter)
}

private fun copyDeviceAddress(
    context: Context,
    address: String
) {
    val clipboardManager =
        context.getSystemService(
            Context.CLIPBOARD_SERVICE
        ) as ClipboardManager

    val clipData =
        ClipData.newPlainText(
            "Oreo Smart Outlet address",
            address
        )

    clipboardManager.setPrimaryClip(
        clipData
    )
}

private const val FEEDBACK_DURATION_MILLISECONDS =
    3_500L

@Preview(
    name = "Diagnostics Large Font",
    widthDp = 360,
    fontScale = 2f,
    showBackground = true,
    backgroundColor = 0xFF0F172A
)
@Composable
private fun DiagnosticsLargeFontPreview() {
    OreoSmartOutletTheme(
        darkTheme = true
    ) {
        Surface(
            color =
                MaterialTheme.colorScheme
                    .background
        ) {
            Column(
                modifier =
                    Modifier.padding(
                        OreoSpacing.ScreenMargin
                    ),
                verticalArrangement =
                    Arrangement.spacedBy(
                        OreoSpacing.StackMedium
                    )
            ) {
                DeviceHealthSummaryCard(
                    state =
                        DeviceStatusRepositoryState(
                            connectionState =
                                DeviceConnectionState
                                    .OFFLINE
                        ),
                    onCopyDeviceAddress = {}
                )

                DiagnosticsActionsCard(
                    state = DiagnosticsUiState(),
                    feedbackMessage = null,
                    feedbackIsError = false,
                    onRefreshDeviceStatus = {},
                    onRequestTimeSync = {},
                    onManageDevice = {}
                )
            }
        }
    }
}
