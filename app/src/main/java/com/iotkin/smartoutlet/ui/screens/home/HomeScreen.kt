package com.iotkin.smartoutlet.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iotkin.smartoutlet.data.model.RelayNumber
import com.iotkin.smartoutlet.data.model.RelayScheduleResponse
import com.iotkin.smartoutlet.data.model.RelayStatusResponse
import com.iotkin.smartoutlet.data.repository.DeviceConnectionState
import com.iotkin.smartoutlet.data.repository.DeviceStatusRepositoryState
import com.iotkin.smartoutlet.ui.screens.diagnostics.DiagnosticsViewModel
import com.iotkin.smartoutlet.ui.theme.OreoShapeTokens
import com.iotkin.smartoutlet.ui.theme.OreoSpacing
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay
import com.iotkin.smartoutlet.ui.components.connectionDescription
import com.iotkin.smartoutlet.ui.components.connectionLabel
import com.iotkin.smartoutlet.ui.components.writeUnavailableMessage

@Composable
fun HomeRoute(
    viewModel: DiagnosticsViewModel,
    onOutletSelected: (RelayNumber) -> Unit,
    modifier: Modifier = Modifier
) {
    val statusState by viewModel.statusState
        .collectAsStateWithLifecycle()

    val relayControlState by viewModel.relayControlState
        .collectAsStateWithLifecycle()

    LaunchedEffect(
        relayControlState.message,
        relayControlState.error
    ) {
        if (
            relayControlState.message != null ||
            relayControlState.error != null
        ) {
            delay(RELAY_FEEDBACK_DURATION_MILLISECONDS)
            viewModel.clearRelayFeedback()
        }
    }

    HomeScreen(
        statusState = statusState,
        relayControlState = relayControlState,
        onOutletStateChange = viewModel::setRelayState,
        onOutletSelected = onOutletSelected,
        modifier = modifier
    )
}

@Composable
fun HomeScreen(
    statusState: DeviceStatusRepositoryState,
    relayControlState: RelayControlUiState,
    onOutletStateChange: (
        RelayNumber,
        Boolean
    ) -> Unit,
    onOutletSelected: (RelayNumber) -> Unit,
    modifier: Modifier = Modifier
) {
    val status = statusState.status

    val deviceAvailable =
        status != null &&
                statusState.connectionState ==
                DeviceConnectionState.ONLINE &&
                !statusState.isStale

    val controlsEnabled =
        deviceAvailable &&
                !relayControlState.isAnyRelayUpdating

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = OreoSpacing.ScreenMargin,
            top = OreoSpacing.StackMedium,
            end = OreoSpacing.ScreenMargin,
            bottom = OreoSpacing.StackMedium
        ),
        verticalArrangement = Arrangement.spacedBy(
            OreoSpacing.StackMedium
        )
    ) {
        item {
            HomeHeader(
                statusState = statusState,
                philippineTime =
                    status?.philippineTime
            )
        }

        when {
            statusState.isInitialLoading -> {
                item {
                    HomeLoadingCard()
                }
            }

            status == null -> {
                item {
                    HomeUnavailableCard(
                        statusState = statusState
                    )
                }
            }

            else -> {
                item {
                    Text(
                        text = "Your outlets",
                        style =
                            MaterialTheme.typography
                                .headlineMedium,
                        color =
                            MaterialTheme.colorScheme
                                .onBackground
                    )
                }

                item {
                    OutletControlCard(
                        relay = RelayNumber.RELAY_1,
                        relayStatus = status.relay1,
                        statusState = statusState,
                        controlsEnabled =
                            controlsEnabled,
                        deviceAvailable =
                            deviceAvailable,
                        isUpdating =
                            relayControlState.isUpdating(
                                RelayNumber.RELAY_1
                            ),
                        lastRefreshEpochMillis =
                            statusState
                                .lastSuccessfulRefreshEpochMillis,
                        onStateChange = {
                                desiredState ->
                            onOutletStateChange(
                                RelayNumber.RELAY_1,
                                desiredState
                            )
                        },
                        onOpenDetails = {
                            onOutletSelected(
                                RelayNumber.RELAY_1
                            )
                        }
                    )
                }

                item {
                    OutletControlCard(
                        relay = RelayNumber.RELAY_2,
                        relayStatus = status.relay2,
                        statusState = statusState,
                        controlsEnabled =
                            controlsEnabled,
                        deviceAvailable =
                            deviceAvailable,
                        isUpdating =
                            relayControlState.isUpdating(
                                RelayNumber.RELAY_2
                            ),
                        lastRefreshEpochMillis =
                            statusState
                                .lastSuccessfulRefreshEpochMillis,
                        onStateChange = {
                                desiredState ->
                            onOutletStateChange(
                                RelayNumber.RELAY_2,
                                desiredState
                            )
                        },
                        onOpenDetails = {
                            onOutletSelected(
                                RelayNumber.RELAY_2
                            )
                        }
                    )
                }

                val feedbackMessage =
                    relayControlState.error
                        ?: relayControlState.message

                feedbackMessage?.let { message ->
                    item {
                        InlineOutletFeedback(
                            message = message,
                            isError =
                                relayControlState.error != null
                        )
                    }
                }

                item {
                    PhilippineTimeCard(
                        philippineTime =
                            status.philippineTime,
                        timeValid =
                            status.timeValid
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.spacedBy(
                                OreoSpacing.StackMedium
                            )
                    ) {
                        DeviceMetricCard(
                            title = "Wi-Fi signal",
                            value = "${status.rssi} dBm",
                            supportingText =
                                status.ssid.ifBlank {
                                    "Network not reported"
                                },
                            modifier =
                                Modifier.weight(1f)
                        )

                        DeviceMetricCard(
                            title = "Device uptime",
                            value =
                                formatOutletUptime(
                                    status.uptimeSeconds
                                ),
                            supportingText =
                                "Since last restart",
                            modifier =
                                Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(
    statusState: DeviceStatusRepositoryState,
    philippineTime: String?
) {
    val greeting =
        greetingFromPhilippineTime(
            philippineTime
        )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(
            OreoSpacing.StackSmall
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
                        OreoSpacing.StackSmall
                    )
            ) {
                Text(
                    text = greeting,
                    style =
                        MaterialTheme.typography
                            .headlineLarge,
                    color =
                        MaterialTheme.colorScheme
                            .onBackground
                )

                Text(
                    text =
                        statusState.connectionDescription(),
                    style =
                        MaterialTheme.typography
                            .bodyLarge,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )
            }

            ConnectionStatusPill(
                statusState = statusState
            )
        }
    }
}

@Composable
private fun ConnectionStatusPill(
    statusState: DeviceStatusRepositoryState
) {
    val text =
        statusState.connectionLabel()

    val color =
        when {
            statusState.isStale ->
                MaterialTheme.colorScheme.error

            statusState.connectionState ==
                    DeviceConnectionState.ONLINE ->
                MaterialTheme.colorScheme.primary

            statusState.connectionState ==
                    DeviceConnectionState.CONNECTING ||
                    statusState.connectionState ==
                    DeviceConnectionState.RECONNECTING ->
                MaterialTheme.colorScheme.tertiary

            else ->
                MaterialTheme.colorScheme.error
        }

    Surface(
        shape = OreoShapeTokens.ExtraLarge,
        color = color.copy(alpha = 0.10f),
        border = BorderStroke(
            width = 1.dp,
            color = color.copy(alpha = 0.35f)
        )
    ) {
        Text(
            text = text,
            style =
                MaterialTheme.typography
                    .labelLarge,
            color = color,
            modifier = Modifier.padding(
                horizontal =
                    OreoSpacing.StackMedium,
                vertical =
                    OreoSpacing.StackSmall
            )
        )
    }
}

@Composable
private fun OutletControlCard(
    relay: RelayNumber,
    relayStatus: RelayStatusResponse,
    statusState:
    DeviceStatusRepositoryState,
    controlsEnabled: Boolean,
    deviceAvailable: Boolean,
    isUpdating: Boolean,
    lastRefreshEpochMillis: Long?,
    onStateChange: (Boolean) -> Unit,
    onOpenDetails: () -> Unit
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
                            OreoSpacing.StackSmall
                        )
                ) {
                    Text(
                        text =
                            "OUTLET ${relay.apiValue}",
                        style =
                            MaterialTheme.typography
                                .labelLarge,
                        color =
                            MaterialTheme.colorScheme
                                .primary
                    )

                    Text(
                        text =
                            "Outlet ${relay.apiValue}",
                        style =
                            MaterialTheme.typography
                                .headlineMedium,
                        color =
                            MaterialTheme.colorScheme
                                .onSurface
                    )
                }

                if (isUpdating) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(
                            OreoSpacing.StackSmall
                        ),
                        strokeWidth = 2.dp
                    )
                } else {
                    Switch(
                        checked = relayStatus.state,
                        onCheckedChange =
                            onStateChange,
                        enabled = controlsEnabled
                    )
                }
            }

            HorizontalDivider(
                color =
                    MaterialTheme.colorScheme
                        .outlineVariant
                        .copy(alpha = 0.30f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(
                            OreoSpacing.StackSmall
                        )
                ) {
                    Text(
                        text =
                            if (isUpdating) {
                                "Updating..."
                            } else if (
                                relayStatus.state
                            ) {
                                "ON"
                            } else {
                                "OFF"
                            },
                        style =
                            MaterialTheme.typography
                                .titleMedium,
                        fontWeight =
                            FontWeight.SemiBold,
                        color =
                            if (
                                relayStatus.state
                            ) {
                                MaterialTheme
                                    .colorScheme
                                    .primary
                            } else {
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                            }
                    )

                    Text(
                        text = "Current state",
                        style =
                            MaterialTheme.typography
                                .bodySmall,
                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )
                }

                Column(
                    horizontalAlignment =
                        Alignment.End,
                    verticalArrangement =
                        Arrangement.spacedBy(
                            OreoSpacing.StackSmall
                        )
                ) {
                    Text(
                        text =
                            scheduleSummary(
                                relayStatus.schedule
                            ),
                        style =
                            MaterialTheme.typography
                                .bodyMedium,
                        textAlign = TextAlign.End,
                        color =
                            MaterialTheme.colorScheme
                                .onSurface
                    )

                    Text(
                        text =
                            formatLastAppRefresh(
                                lastRefreshEpochMillis
                            ),
                        style =
                            MaterialTheme.typography
                                .bodySmall,
                        textAlign = TextAlign.End,
                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )
                }
            }

            if (!deviceAvailable) {
                Text(
                    text =
                        statusState
                            .writeUnavailableMessage(
                                featureName =
                                    "Outlet controls"
                            )
                            ?: "",
                    style =
                        MaterialTheme.typography
                            .bodySmall,
                    color =
                        MaterialTheme.colorScheme
                            .error
                )
            }

            TextButton(
                onClick = onOpenDetails,
                modifier = Modifier.align(
                    Alignment.End
                )
            ) {
                Text(
                    text = "View details"
                )
            }
        }
    }
}

@Composable
private fun PhilippineTimeCard(
    philippineTime: String,
    timeValid: Boolean
) {
    val formatted =
        formatPhilippineTime(
            philippineTime
        )

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
                    OreoSpacing.StackSmall
                )
        ) {
            Text(
                text = "Philippine Time",
                style =
                    MaterialTheme.typography
                        .titleMedium,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )

            Text(
                text = formatted.first,
                style =
                    MaterialTheme.typography
                        .displayMedium,
                fontWeight = FontWeight.Bold,
                color =
                    MaterialTheme.colorScheme
                        .onSurface
            )

            Text(
                text = formatted.second,
                style =
                    MaterialTheme.typography
                        .bodyLarge,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )

            if (!timeValid) {
                Text(
                    text =
                        "Device time is not synchronized.",
                    style =
                        MaterialTheme.typography
                            .bodySmall,
                    color =
                        MaterialTheme.colorScheme
                            .error
                )
            }
        }
    }
}

@Composable
private fun DeviceMetricCard(
    title: String,
    value: String,
    supportingText: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
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
                    OreoSpacing.StackSmall
                )
        ) {
            Text(
                text = title,
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
                        .headlineMedium,
                fontWeight = FontWeight.Bold,
                color =
                    MaterialTheme.colorScheme
                        .onSurface
            )

            Text(
                text = supportingText,
                style =
                    MaterialTheme.typography
                        .bodySmall,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InlineOutletFeedback(
    message: String,
    isError: Boolean
) {
    Text(
        text = message,
        style =
            MaterialTheme.typography.bodyMedium,
        color =
            if (isError) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            },
        textAlign = TextAlign.End,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun HomeLoadingCard() {
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

            Text(
                text = "Loading outlet status...",
                style =
                    MaterialTheme.typography
                        .bodyLarge
            )
        }
    }
}

@Composable
private fun HomeUnavailableCard(
    statusState: DeviceStatusRepositoryState
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
                    .error
                    .copy(alpha = 0.30f)
        )
    ) {
        Column(
            modifier = Modifier.padding(
                OreoSpacing.CardPadding
            ),
            horizontalAlignment =
                Alignment.End,
            verticalArrangement =
                Arrangement.spacedBy(
                    OreoSpacing.StackSmall
                )
        ) {
            Text(
                text = "Outlet status unavailable",
                style =
                    MaterialTheme.typography
                        .titleMedium,
                color =
                    MaterialTheme.colorScheme
                        .onSurface,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text =
                    statusState.errorMessage
                        ?: "No confirmed outlet status has been received.",
                style =
                    MaterialTheme.typography
                        .bodyMedium,
                textAlign = TextAlign.End,
                color =
                    MaterialTheme.colorScheme
                        .error
            )
        }
    }
}

internal fun formatLastAppRefresh(
    epochMillis: Long?
): String {
    if (epochMillis == null) {
        return "Not refreshed"
    }

    val formatter =
        DateTimeFormatter.ofPattern(
            "MMM d, h:mm:ss a",
            Locale.US
        )

    return "Updated " +
            Instant
                .ofEpochMilli(epochMillis)
                .atZone(ZoneId.systemDefault())
                .format(formatter)
}

internal fun scheduleSummary(
    schedule: RelayScheduleResponse
): String {
    if (!schedule.enabled) {
        return "Schedule disabled"
    }

    return "${formatScheduleClock(schedule.onHour, schedule.onMinute)} – " +
            formatScheduleClock(
                schedule.offHour,
                schedule.offMinute
            )
}

internal fun formatScheduleClock(
    hour: Int,
    minute: Int
): String {
    val normalizedHour =
        hour.coerceIn(0, 23)

    val suffix =
        if (normalizedHour < 12) {
            "AM"
        } else {
            "PM"
        }

    val twelveHour =
        when (val value = normalizedHour % 12) {
            0 -> 12
            else -> value
        }

    return String.format(
        Locale.US,
        "%d:%02d %s",
        twelveHour,
        minute.coerceIn(0, 59),
        suffix
    )
}

internal fun formatOutletUptime(
    totalSeconds: Long
): String {
    val days =
        totalSeconds / 86_400

    val hours =
        totalSeconds % 86_400 / 3_600

    val minutes =
        totalSeconds % 3_600 / 60

    return when {
        days > 0 ->
            "${days}d ${hours}h"

        hours > 0 ->
            "${hours}h ${minutes}m"

        else ->
            "${minutes}m"
    }
}

private fun greetingFromPhilippineTime(
    value: String?
): String {
    val hour =
        value
            ?.let {
                runCatching {
                    OffsetDateTime
                        .parse(it)
                        .hour
                }.getOrNull()
            }

    return when (hour) {
        in 5..11 -> "Good morning!"
        in 12..17 -> "Good afternoon!"
        in 18..23,
        in 0..4 -> "Good evening!"
        else -> "Welcome back!"
    }
}

private fun formatPhilippineTime(
    value: String
): Pair<String, String> {
    val parsed =
        runCatching {
            OffsetDateTime.parse(value)
        }.getOrNull()

    if (parsed == null) {
        return Pair(
            value.ifBlank {
                "Time unavailable"
            },
            "Philippine Time"
        )
    }

    val timeFormatter =
        DateTimeFormatter.ofPattern(
            "h:mm:ss a",
            Locale.US
        )

    val dateFormatter =
        DateTimeFormatter.ofPattern(
            "EEEE, MMMM d, yyyy",
            Locale.US
        )

    return Pair(
        parsed.format(timeFormatter),
        parsed.format(dateFormatter)
    )
}

private const val
        RELAY_FEEDBACK_DURATION_MILLISECONDS =
    3_500L