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
import com.iotkin.smartoutlet.data.model.RelayStatusResponse
import com.iotkin.smartoutlet.data.repository.DeviceConnectionState
import com.iotkin.smartoutlet.ui.screens.diagnostics.DiagnosticsViewModel
import com.iotkin.smartoutlet.ui.theme.OreoShapeTokens
import com.iotkin.smartoutlet.ui.theme.OreoSpacing
import kotlinx.coroutines.delay
import com.iotkin.smartoutlet.data.repository.DeviceStatusRepositoryState
import com.iotkin.smartoutlet.ui.components.writeUnavailableMessage

@Composable
fun OutletDetailsRoute(
    relay: RelayNumber,
    viewModel: DiagnosticsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusState by viewModel.statusState
        .collectAsStateWithLifecycle()

    val relayControlState by viewModel.relayControlState
        .collectAsStateWithLifecycle()

    val appSettings by viewModel.appSettings
        .collectAsStateWithLifecycle()

    LaunchedEffect(
        relayControlState.message,
        relayControlState.error
    ) {
        if (
            relayControlState.message != null ||
            relayControlState.error != null
        ) {
            delay(3_500L)
            viewModel.clearRelayFeedback()
        }
    }

    val status = statusState.status

    val relayStatus =
        when (relay) {
            RelayNumber.RELAY_1 ->
                status?.relay1

            RelayNumber.RELAY_2 ->
                status?.relay2
        }

    val deviceAvailable =
        status != null &&
                statusState.connectionState ==
                DeviceConnectionState.ONLINE &&
                !statusState.isStale

    val controlsEnabled =
        deviceAvailable &&
                !relayControlState.isAnyRelayUpdating

    OutletDetailsScreen(
        statusState = statusState,
        relay = relay,
        outletName =
            appSettings
                .outletFriendlyName(relay),
        relayStatus = relayStatus,
        controlsEnabled =
            controlsEnabled,
        deviceAvailable =
            deviceAvailable,
        isUpdating =
            relayControlState.isUpdating(relay),
        isStale =
            statusState.isStale,
        lastRefreshEpochMillis =
            statusState
                .lastSuccessfulRefreshEpochMillis,
        feedbackMessage =
            relayControlState.error
                ?: relayControlState.message,
        feedbackIsError =
            relayControlState.error != null,
        errorMessage =
            statusState.errorMessage,
        onStateChange = {
                desiredState ->
            viewModel.setRelayState(
                relay = relay,
                desiredState = desiredState
            )
        },
        onBack = onBack,
        modifier = modifier
    )
}

@Composable
fun OutletDetailsScreen(
    relay: RelayNumber,
    outletName: String,
    relayStatus: RelayStatusResponse?,
    statusState:
    DeviceStatusRepositoryState,
    controlsEnabled: Boolean,
    deviceAvailable: Boolean,
    isUpdating: Boolean,
    isStale: Boolean,
    lastRefreshEpochMillis: Long?,
    feedbackMessage: String?,
    feedbackIsError: Boolean,
    errorMessage: String?,
    onStateChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = OreoSpacing.ScreenMargin,
            top = OreoSpacing.StackMedium,
            end = OreoSpacing.ScreenMargin,
            bottom = OreoSpacing.StackMedium
        ),
        verticalArrangement =
            Arrangement.spacedBy(
                OreoSpacing.StackMedium
            )
    ) {
        item {
            TextButton(
                onClick = onBack
            ) {
                Text(
                    text = "Back"
                )
            }
        }

        item {
            Column(
                verticalArrangement =
                    Arrangement.spacedBy(
                        OreoSpacing.StackSmall
                    )
            ) {
                Text(
                    text = outletName,
                    style =
                        MaterialTheme.typography
                            .headlineLarge,
                    color =
                        MaterialTheme.colorScheme
                            .onBackground
                )

                Text(
                    text =
                        "Live state and schedule information.",
                    style =
                        MaterialTheme.typography
                            .bodyLarge,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )
            }
        }

        if (relayStatus == null) {
            item {
                Surface(
                    modifier =
                        Modifier.fillMaxWidth(),
                    shape =
                        OreoShapeTokens.ExtraLarge,
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
                            text =
                                "Waiting for confirmed outlet status...",
                            style =
                                MaterialTheme.typography
                                    .bodyLarge
                        )
                    }
                }
            }
        } else {
            item {
                OutletStateDetailsCard(
                    relay = relay,
                    relayStatus = relayStatus,
                    statusState = statusState,
                    controlsEnabled =
                        controlsEnabled,
                    deviceAvailable =
                        deviceAvailable,
                    isUpdating = isUpdating,
                    isStale = isStale,
                    lastRefreshEpochMillis =
                        lastRefreshEpochMillis,
                    feedbackMessage =
                        feedbackMessage,
                    feedbackIsError =
                        feedbackIsError,
                    errorMessage =
                        errorMessage,
                    onStateChange =
                        onStateChange
                )
            }

            item {
                OutletScheduleDetailsCard(
                    relayStatus = relayStatus
                )
            }
        }
    }
}

@Composable
private fun OutletStateDetailsCard(
    relay: RelayNumber,
    relayStatus: RelayStatusResponse,
    statusState:
    DeviceStatusRepositoryState,
    controlsEnabled: Boolean,
    deviceAvailable: Boolean,
    isUpdating: Boolean,
    isStale: Boolean,
    lastRefreshEpochMillis: Long?,
    feedbackMessage: String?,
    feedbackIsError: Boolean,
    errorMessage: String?,
    onStateChange: (Boolean) -> Unit
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
                                .headlineMedium,
                        fontWeight =
                            FontWeight.Bold,
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
                                    .onSurface
                            }
                    )
                }

                if (isUpdating) {
                    CircularProgressIndicator(
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

            HorizontalDivider()

            DetailsValueRow(
                label = "Current state",
                value =
                    if (relayStatus.state) {
                        "On"
                    } else {
                        "Off"
                    }
            )

            DetailsValueRow(
                label =
                    "Last successful app refresh",
                value =
                    formatLastAppRefresh(
                        lastRefreshEpochMillis
                    )
            )

            if (!deviceAvailable) {
                Text(
                    text =
                        errorMessage
                            ?: statusState
                                .writeUnavailableMessage(
                                    featureName =
                                        "Outlet controls"
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

            feedbackMessage?.let { message ->
                Text(
                    text = message,
                    style =
                        MaterialTheme.typography
                            .bodyMedium,
                    color =
                        if (feedbackIsError) {
                            MaterialTheme
                                .colorScheme
                                .error
                        } else {
                            MaterialTheme
                                .colorScheme
                                .primary
                        },
                    textAlign = TextAlign.End,
                    modifier =
                        Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun OutletScheduleDetailsCard(
    relayStatus: RelayStatusResponse
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
            Text(
                text = "Current schedule",
                style =
                    MaterialTheme.typography
                        .headlineMedium,
                color =
                    MaterialTheme.colorScheme
                        .onSurface
            )

            DetailsValueRow(
                label = "Enabled",
                value =
                    if (schedule.enabled) {
                        "Yes"
                    } else {
                        "No"
                    }
            )

            DetailsValueRow(
                label = "Turn on",
                value =
                    formatScheduleClock(
                        schedule.onHour,
                        schedule.onMinute
                    )
            )

            DetailsValueRow(
                label = "Turn off",
                value =
                    formatScheduleClock(
                        schedule.offHour,
                        schedule.offMinute
                    )
            )

            Text(
                text =
                    "Manual changes remain active until the next scheduled event.",
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
private fun DetailsValueRow(
    label: String,
    value: String
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
            textAlign = TextAlign.End,
            color =
                MaterialTheme.colorScheme
                    .onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}
