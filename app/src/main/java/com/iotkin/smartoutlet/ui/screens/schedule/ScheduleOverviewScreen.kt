package com.iotkin.smartoutlet.ui.screens.schedule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iotkin.smartoutlet.data.model.RelayNumber
import com.iotkin.smartoutlet.data.model.RelayScheduleResponse
import com.iotkin.smartoutlet.data.model.RelayStatusResponse
import com.iotkin.smartoutlet.data.repository.DeviceConnectionState
import com.iotkin.smartoutlet.data.repository.DeviceStatusRepositoryState
import com.iotkin.smartoutlet.data.settings.TimeFormatPreference
import com.iotkin.smartoutlet.ui.screens.diagnostics.DiagnosticsViewModel
import com.iotkin.smartoutlet.ui.theme.OreoShapeTokens
import com.iotkin.smartoutlet.ui.theme.OreoSmartOutletTheme
import com.iotkin.smartoutlet.ui.theme.OreoSpacing
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.iotkin.smartoutlet.ui.components.connectionDescription
import com.iotkin.smartoutlet.ui.components.connectionLabel
import com.iotkin.smartoutlet.ui.components.writeUnavailableMessage

@Composable
fun ScheduleOverviewRoute(
    viewModel: DiagnosticsViewModel,
    onEditSchedule: (RelayNumber) -> Unit,
    modifier: Modifier = Modifier
) {
    val statusState by viewModel.statusState
        .collectAsStateWithLifecycle()

    val appSettings by viewModel.appSettings
        .collectAsStateWithLifecycle()

    ScheduleOverviewScreen(
        statusState = statusState,
        outlet1Name =
            appSettings
                .outlet1FriendlyName,
        outlet2Name =
            appSettings
                .outlet2FriendlyName,
        timeFormatPreference =
            appSettings.timeFormatPreference,
        onEditSchedule = onEditSchedule,
        modifier = modifier
    )
}

@Composable
fun ScheduleOverviewScreen(
    statusState: DeviceStatusRepositoryState,
    outlet1Name: String,
    outlet2Name: String,
    timeFormatPreference:
        TimeFormatPreference,
    onEditSchedule: (RelayNumber) -> Unit,
    modifier: Modifier = Modifier
) {
    val status = statusState.status

    val deviceAvailable =
        status != null &&
                statusState.connectionState ==
                DeviceConnectionState.ONLINE &&
                !statusState.isStale

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
            ScheduleHeader(
                statusState = statusState
            )
        }

        when {
            statusState.isInitialLoading -> {
                item {
                    ScheduleLoadingCard()
                }
            }

            status == null -> {
                item {
                    ScheduleUnavailableCard(
                        message =
                            statusState.errorMessage
                                ?: "Confirmed schedule data is not available."
                    )
                }
            }

            else -> {
                item {
                    OutletScheduleCard(
                        relay = RelayNumber.RELAY_1,
                        outletName = outlet1Name,
                        relayStatus = status.relay1,
                        deviceAvailable = deviceAvailable,
                        lastRefreshEpochMillis =
                            statusState
                                .lastSuccessfulRefreshEpochMillis,
                        timeFormatPreference =
                            timeFormatPreference,
                        onEdit = {
                            onEditSchedule(
                                RelayNumber.RELAY_1
                            )
                        }
                    )
                }

                item {
                    OutletScheduleCard(
                        relay = RelayNumber.RELAY_2,
                        outletName = outlet2Name,
                        relayStatus = status.relay2,
                        deviceAvailable = deviceAvailable,
                        lastRefreshEpochMillis =
                            statusState
                                .lastSuccessfulRefreshEpochMillis,
                        timeFormatPreference =
                            timeFormatPreference,
                        onEdit = {
                            onEditSchedule(
                                RelayNumber.RELAY_2
                            )
                        }
                    )
                }

                if (!deviceAvailable) {
                    item {
                        Text(
                            text =
                                statusState
                                    .writeUnavailableMessage(
                                        featureName =
                                            "Schedule editing"
                                    )
                                    ?: "",
                            style =
                                MaterialTheme.typography
                                    .bodyMedium,
                            color =
                                MaterialTheme.colorScheme
                                    .error,
                            textAlign = TextAlign.End,
                            modifier =
                                Modifier.fillMaxWidth()
                        )
                    }
                }

                item {
                    Text(
                        text =
                            "Next-event information is hidden because the device does not provide a confirmed next scheduled event.",
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
    }
}

@Composable
private fun ScheduleHeader(
    statusState: DeviceStatusRepositoryState
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement =
            Arrangement.spacedBy(
                OreoSpacing.StackSmall
            )
    ) {
        Text(
            text = "Schedules",
            style =
                MaterialTheme.typography
                    .headlineLarge,
            color =
                MaterialTheme.colorScheme
                    .onBackground
        )

        Text(
            text =
                "Set independent ON and OFF times for each outlet.",

            style =
                MaterialTheme.typography
                    .bodyLarge,
            color =
                MaterialTheme.colorScheme
                    .onSurfaceVariant
        )

        Text(
            text =
                statusState.connectionDescription(),
            style =
                MaterialTheme.typography
                    .bodySmall,
            color =
                MaterialTheme.colorScheme
                    .onSurfaceVariant
        )

        ScheduleConnectionBadge(
            statusState = statusState,
            modifier =
                Modifier.align(Alignment.End)
        )
    }
}

@Composable
private fun ScheduleConnectionBadge(
    statusState: DeviceStatusRepositoryState,
    modifier: Modifier = Modifier
) {
    val label =
        statusState.connectionLabel()

    val badgeColor =
        when {
            statusState.connectionState ==
                    DeviceConnectionState.ONLINE &&
                    !statusState.isStale ->
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
        modifier = modifier,
        shape = OreoShapeTokens.ExtraLarge,
        color = badgeColor.copy(
            alpha = 0.10f
        ),
        border = BorderStroke(
            width = 1.dp,
            color = badgeColor.copy(
                alpha = 0.35f
            )
        )
    ) {
        Text(
            text = label,
            style =
                MaterialTheme.typography
                    .labelLarge,
            color = badgeColor,
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
private fun OutletScheduleCard(
    relay: RelayNumber,
    outletName: String,
    relayStatus: RelayStatusResponse,
    deviceAvailable: Boolean,
    lastRefreshEpochMillis: Long?,
    timeFormatPreference:
        TimeFormatPreference,
    onEdit: () -> Unit
) {
    val schedule =
        relayStatus.schedule

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

                ScheduleEnabledBadge(
                    enabled = schedule.enabled
                )
            }

            Text(
                text = outletName,
                style =
                    MaterialTheme.typography
                        .headlineMedium,
                color =
                    MaterialTheme.colorScheme
                        .onSurface
            )

            ScheduleTimeRow(
                label = "Turn on",
                value = formatScheduleTime(
                    hour = schedule.onHour,
                    minute = schedule.onMinute,
                    timeFormatPreference =
                        timeFormatPreference
                )
            )

            ScheduleTimeRow(
                label = "Turn off",
                value = formatScheduleTime(
                    hour = schedule.offHour,
                    minute = schedule.offMinute,
                    timeFormatPreference =
                        timeFormatPreference
                )
            )

            ScheduleTimeRow(
                label = "Current outlet state",
                value =
                    if (relayStatus.state) {
                        "ON"
                    } else {
                        "OFF"
                    }
            )

            Text(
                text =
                    formatScheduleRefresh(
                        epochMillis =
                            lastRefreshEpochMillis,
                        timeFormatPreference =
                            timeFormatPreference
                    ),
                style =
                    MaterialTheme.typography
                        .bodySmall,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )

            if (!schedule.enabled) {
                Text(
                    text =
                        "This schedule is disabled. Its saved times remain stored.",
                    style =
                        MaterialTheme.typography
                            .bodySmall,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )
            }

            Button(
                onClick = onEdit,
                enabled = deviceAvailable,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Edit schedule"
                )
            }
        }
    }
}

@Composable
private fun ScheduleEnabledBadge(
    enabled: Boolean
) {
    val badgeColor =
        if (enabled) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme
                .onSurfaceVariant
        }

    Surface(
        shape = OreoShapeTokens.ExtraLarge,
        color = badgeColor.copy(
            alpha = 0.10f
        )
    ) {
        Text(
            text =
                if (enabled) {
                    "Enabled"
                } else {
                    "Disabled"
                },
            style =
                MaterialTheme.typography
                    .labelLarge,
            color = badgeColor,
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
private fun ScheduleTimeRow(
    label: String,
    value: String
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth()
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
                            .bodyLarge,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )

                Text(
                    text = value,
                    style =
                        MaterialTheme.typography
                            .titleMedium,
                    fontWeight =
                        FontWeight.SemiBold,
                    color =
                        MaterialTheme.colorScheme
                            .onSurface
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
                    Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style =
                        MaterialTheme.typography
                            .bodyLarge,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = value,
                    style =
                        MaterialTheme.typography
                            .titleMedium,
                    fontWeight =
                        FontWeight.SemiBold,
                    textAlign = TextAlign.End,
                    color =
                        MaterialTheme.colorScheme
                            .onSurface,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ScheduleLoadingCard() {
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
                text = "Loading schedules...",
                style =
                    MaterialTheme.typography
                        .bodyLarge
            )
        }
    }
}

@Composable
private fun ScheduleUnavailableCard(
    message: String
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
            verticalArrangement =
                Arrangement.spacedBy(
                    OreoSpacing.StackSmall
                )
        ) {
            Text(
                text = "Schedules unavailable",
                style =
                    MaterialTheme.typography
                        .titleMedium
            )

            Text(
                text = message,
                style =
                    MaterialTheme.typography
                        .bodyMedium,
                color =
                    MaterialTheme.colorScheme
                        .error
            )
        }
    }
}

internal fun formatScheduleTime(
    hour: Int,
    minute: Int,
    timeFormatPreference:
        TimeFormatPreference
): String {
    val safeHour =
        hour.coerceIn(0, 23)

    val safeMinute =
        minute.coerceIn(0, 59)

    if (
        timeFormatPreference ==
        TimeFormatPreference
            .TWENTY_FOUR_HOUR
    ) {
        return String.format(
            Locale.US,
            "%02d:%02d",
            safeHour,
            safeMinute
        )
    }

    val suffix =
        if (safeHour < 12) {
            "AM"
        } else {
            "PM"
        }

    val displayHour =
        when (val value = safeHour % 12) {
            0 -> 12
            else -> value
        }

    return String.format(
        Locale.US,
        "%d:%02d %s",
        displayHour,
        safeMinute,
        suffix
    )
}

private fun formatScheduleRefresh(
    epochMillis: Long?,
    timeFormatPreference:
        TimeFormatPreference
): String {
    if (epochMillis == null) {
        return "Not refreshed"
    }

    val formatter =
        DateTimeFormatter.ofPattern(
            when (timeFormatPreference) {
                TimeFormatPreference
                    .TWELVE_HOUR ->
                    "MMM d, h:mm:ss a"

                TimeFormatPreference
                    .TWENTY_FOUR_HOUR ->
                    "MMM d, HH:mm:ss"
            },
            Locale.US
        )

    val formatted =
        Instant
            .ofEpochMilli(epochMillis)
            .atZone(ZoneId.systemDefault())
            .format(formatter)

    return "Last app refresh: $formatted"
}

@Preview(
    name = "Schedule Card Large Font",
    widthDp = 360,
    fontScale = 2f,
    showBackground = true,
    backgroundColor = 0xFF0F172A
)
@Composable
private fun OutletScheduleCardLargeFontPreview() {
    OreoSmartOutletTheme(
        darkTheme = true
    ) {
        Surface(
            modifier =
                Modifier.padding(
                    OreoSpacing.ScreenMargin
                ),
            color =
                MaterialTheme.colorScheme
                    .background
        ) {
            OutletScheduleCard(
                relay = RelayNumber.RELAY_1,
                outletName = "Mosquito Repellent",
                relayStatus =
                    RelayStatusResponse(
                        state = false,
                        schedule =
                            RelayScheduleResponse(
                                enabled = true,
                                onHour = 20,
                                onMinute = 0,
                                offHour = 5,
                                offMinute = 0
                            )
                    ),
                deviceAvailable = true,
                lastRefreshEpochMillis = null,
                timeFormatPreference =
                    TimeFormatPreference
                        .TWELVE_HOUR,
                onEdit = {}
            )
        }
    }
}
