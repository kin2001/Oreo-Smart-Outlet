package com.iotkin.smartoutlet.ui.screens.diagnostics

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iotkin.smartoutlet.data.model.DeviceStatusResponse
import com.iotkin.smartoutlet.data.repository.DeviceConnectionState
import com.iotkin.smartoutlet.data.repository.DeviceStatusRepositoryState
import com.iotkin.smartoutlet.ui.theme.OreoShapeTokens
import com.iotkin.smartoutlet.ui.theme.OreoSpacing
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun DiagnosticsRoute(
    viewModel: DiagnosticsViewModel,
    onRunDiscoveryAgain: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState
        .collectAsStateWithLifecycle()

    val context = LocalContext.current

    DiagnosticsScreen(
        state = state,
        onRefreshDeviceStatus =
            viewModel::refreshDeviceStatus,
        onTestConnection =
            viewModel::testConnection,
        onRequestTimeSync =
            viewModel::requestTimeSync,
        onRunDiscoveryAgain =
            onRunDiscoveryAgain,
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
    onRefreshDeviceStatus: () -> Unit,
    onTestConnection: () -> Unit,
    onRequestTimeSync: () -> Unit,
    onRunDiscoveryAgain: () -> Unit,
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
            ConnectionSummaryCard(
                state = repositoryState
            )
        }

        item {
            DiagnosticsActionsCard(
                state = state,
                feedbackMessage = feedbackMessage,
                feedbackIsError = feedbackIsError,
                onRefreshDeviceStatus =
                    onRefreshDeviceStatus,
                onTestConnection =
                    onTestConnection,
                onRequestTimeSync =
                    onRequestTimeSync,
                onRunDiscoveryAgain =
                    onRunDiscoveryAgain,
                onCopyDeviceAddress = {
                    onCopyDeviceAddress()

                    localFeedbackIsError = false
                    localFeedbackMessage =
                        "Device address copied."
                }
            )
        }

        when {
            repositoryState.isInitialLoading -> {
                item {
                    DiagnosticsLoadingCard()
                }
            }

            repositoryState.status == null -> {
                item {
                    DiagnosticsUnavailableCard(
                        state = repositoryState
                    )
                }
            }

            else -> {
                val status =
                    repositoryState.status

                item {
                    NetworkInformationCard(
                        status = status
                    )
                }

                item {
                    TimeInformationCard(
                        status = status
                    )
                }

                item {
                    RelayInformationCard(
                        status = status
                    )
                }

                item {
                    DeviceInformationCard(
                        status = status
                    )
                }

                item {
                    SystemInformationCard(
                        status = status
                    )
                }

                item {
                    ReadinessInformationCard(
                        status = status
                    )
                }
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
                "Live device, network, firmware, and system information.",
            style =
                MaterialTheme.typography
                    .bodyLarge,
            color =
                MaterialTheme.colorScheme
                    .onSurfaceVariant
        )
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
    onTestConnection: () -> Unit,
    onRequestTimeSync: () -> Unit,
    onRunDiscoveryAgain: () -> Unit,
    onCopyDeviceAddress: () -> Unit
) {
    val repositoryState =
        state.repositoryState

    val statusActionRunning =
        state.isRunningStatusAction

    val anyActionRunning =
        statusActionRunning ||
                state.isRequestingTimeSync

    DiagnosticsCard(
        title = "Quick actions"
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
            Text(
                text =
                    if (statusActionRunning) {
                        "Refreshing Status..."
                    } else {
                        "Refresh Status"
                    }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(
                    OreoSpacing.StackSmall
                )
        ) {
            OutlinedButton(
                onClick = onTestConnection,
                enabled = !anyActionRunning,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Test Connection"
                )
            }

            OutlinedButton(
                onClick = onRequestTimeSync,
                enabled =
                    !anyActionRunning &&
                            repositoryState.address != null,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text =
                        if (
                            state.isRequestingTimeSync
                        ) {
                            "Syncing..."
                        } else {
                            "Time Sync"
                        }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(
                    OreoSpacing.StackSmall
                )
        ) {
            OutlinedButton(
                onClick = onCopyDeviceAddress,
                enabled =
                    repositoryState.address != null,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Copy Address"
                )
            }

            OutlinedButton(
                onClick = onRunDiscoveryAgain,
                enabled = !anyActionRunning,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Find Device"
                )
            }
        }

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
    status: DeviceStatusResponse
) {
    DiagnosticsCard(
        title = "Relays and schedules"
    ) {
        Text(
            text = "Relay 1",
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
            text = "Relay 2",
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
private fun DiagnosticsUnavailableCard(
    state: DeviceStatusRepositoryState
) {
    val title =
        if (
            state.connectionState ==
            DeviceConnectionState.NO_SAVED_DEVICE
        ) {
            "No saved device"
        } else {
            "Device status unavailable"
        }

    val message =
        state.errorMessage
            ?: "No confirmed device status has been received."

    MessageCard(
        title = title,
        message = message
    )
}

@Composable
private fun MessageCard(
    title: String,
    message: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OreoShapeTokens.Large,
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
                OreoSpacing.StackMedium
            ),
            verticalArrangement =
                Arrangement.spacedBy(
                    OreoSpacing.StackSmall
                )
        ) {
            Text(
                text = title,
                style =
                    MaterialTheme.typography
                        .titleMedium,
                color =
                    MaterialTheme.colorScheme
                        .onSurface
            )

            InlineStatusMessage(
                message = message,
                isError = true
            )
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
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            style =
                MaterialTheme.typography
                    .bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
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