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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iotkin.smartoutlet.data.model.RelayNumber
import com.iotkin.smartoutlet.data.model.RelayScheduleResponse
import com.iotkin.smartoutlet.data.repository.DeviceConnectionState
import com.iotkin.smartoutlet.data.repository.DeviceStatusRepositoryState
import com.iotkin.smartoutlet.data.settings.AppSettings
import com.iotkin.smartoutlet.data.settings.TimeFormatPreference
import com.iotkin.smartoutlet.ui.components.RelayCard
import com.iotkin.smartoutlet.ui.components.connectionDescription
import com.iotkin.smartoutlet.ui.components.connectionLabel
import com.iotkin.smartoutlet.ui.components.writeUnavailableMessage
import com.iotkin.smartoutlet.ui.screens.diagnostics.DiagnosticsViewModel
import com.iotkin.smartoutlet.ui.theme.OreoShapeTokens
import com.iotkin.smartoutlet.ui.theme.OreoSpacing
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

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
            delay(RELAY_FEEDBACK_DURATION_MILLISECONDS)
            viewModel.clearRelayFeedback()
        }
    }

    HomeScreen(
        statusState = statusState,
        relayControlState = relayControlState,
        appSettings = appSettings,
        onOutletStateChange = viewModel::setRelayState,
        onOutletNameSave =
            viewModel::setOutletFriendlyName,
        onOutletSelected = onOutletSelected,
        modifier = modifier
    )
}

@Composable
fun HomeScreen(
    statusState: DeviceStatusRepositoryState,
    relayControlState: RelayControlUiState,
    appSettings: AppSettings,
    onOutletStateChange: (
        RelayNumber,
        Boolean
    ) -> Unit,
    onOutletNameSave: (
        RelayNumber,
        String
    ) -> Unit,
    onOutletSelected: (RelayNumber) -> Unit,
    modifier: Modifier = Modifier
) {
    var relayBeingRenamed by
    rememberSaveable {
        mutableStateOf<RelayNumber?>(null)
    }

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
                friendlyDeviceName =
                    appSettings
                        .friendlyDeviceName,
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
                    RelayCard(
                        outletLabel = "OUTLET 1",
                        outletName =
                            appSettings
                                .outlet1FriendlyName,
                        isOn = status.relay1.state,
                        scheduleSummary =
                            scheduleSummary(
                                status.relay1
                                    .schedule,
                                appSettings
                                    .timeFormatPreference
                            ),
                        lastUpdated =
                            formatLastAppRefresh(
                                statusState
                                    .lastSuccessfulRefreshEpochMillis,
                                appSettings
                                    .timeFormatPreference
                            ),
                        onToggle = {
                                desiredState ->
                            onOutletStateChange(
                                RelayNumber.RELAY_1,
                                desiredState
                            )
                        },
                        onEditName = {
                            relayBeingRenamed =
                                RelayNumber.RELAY_1
                        },
                        onOpenDetails = {
                            onOutletSelected(
                                RelayNumber.RELAY_1
                            )
                        },
                        controlsEnabled =
                            controlsEnabled,
                        isUpdating =
                            relayControlState.isUpdating(
                                RelayNumber.RELAY_1
                            ),
                        unavailableMessage =
                            if (deviceAvailable) {
                                null
                            } else {
                                statusState
                                    .writeUnavailableMessage(
                                        featureName =
                                            "Outlet controls"
                                    )
                            }
                    )
                }

                item {
                    RelayCard(
                        outletLabel = "OUTLET 2",
                        outletName =
                            appSettings
                                .outlet2FriendlyName,
                        isOn = status.relay2.state,
                        scheduleSummary =
                            scheduleSummary(
                                status.relay2
                                    .schedule,
                                appSettings
                                    .timeFormatPreference
                            ),
                        lastUpdated =
                            formatLastAppRefresh(
                                statusState
                                    .lastSuccessfulRefreshEpochMillis,
                                appSettings
                                    .timeFormatPreference
                            ),
                        onToggle = {
                                desiredState ->
                            onOutletStateChange(
                                RelayNumber.RELAY_2,
                                desiredState
                            )
                        },
                        onEditName = {
                            relayBeingRenamed =
                                RelayNumber.RELAY_2
                        },
                        onOpenDetails = {
                            onOutletSelected(
                                RelayNumber.RELAY_2
                            )
                        },
                        controlsEnabled =
                            controlsEnabled,
                        isUpdating =
                            relayControlState.isUpdating(
                                RelayNumber.RELAY_2
                            ),
                        unavailableMessage =
                            if (deviceAvailable) {
                                null
                            } else {
                                statusState
                                    .writeUnavailableMessage(
                                        featureName =
                                            "Outlet controls"
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
                            status.timeValid,
                        timeFormatPreference =
                            appSettings
                                .timeFormatPreference
                    )
                }
            }
        }
    }

    relayBeingRenamed?.let { relay ->
        RenameOutletDialog(
            relay = relay,
            currentName =
                appSettings
                    .outletFriendlyName(relay),
            onDismiss = {
                relayBeingRenamed = null
            },
            onSave = { newName ->
                onOutletNameSave(
                    relay,
                    newName
                )
                relayBeingRenamed = null
            }
        )
    }
}

@Composable
private fun HomeHeader(
    statusState: DeviceStatusRepositoryState,
    friendlyDeviceName: String,
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
                    text = friendlyDeviceName,
                    style =
                        MaterialTheme.typography
                            .bodyLarge,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )

                if (
                    statusState.isStale ||
                    statusState.connectionState !=
                    DeviceConnectionState.ONLINE
                ) {
                    Text(
                        text =
                            statusState
                                .connectionDescription(),
                        style =
                            MaterialTheme.typography
                                .bodySmall,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }
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
private fun PhilippineTimeCard(
    philippineTime: String,
    timeValid: Boolean,
    timeFormatPreference:
    TimeFormatPreference
) {
    val formatted =
        formatPhilippineTime(
            value = philippineTime,
            timeFormatPreference =
                timeFormatPreference
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
                        .headlineMedium,
                fontWeight = FontWeight.Bold,
                color =
                    MaterialTheme.colorScheme
                        .onSurface
            )

            Text(
                text = formatted.second,
                style =
                    MaterialTheme.typography
                        .bodySmall,
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
                    "h:mm a"

                TimeFormatPreference
                    .TWENTY_FOUR_HOUR ->
                    "HH:mm"
            },
            Locale.US
        )

    return "Updated " +
            Instant
                .ofEpochMilli(epochMillis)
                .atZone(ZoneId.systemDefault())
                .format(formatter)
}

internal fun scheduleSummary(
    schedule: RelayScheduleResponse,
    timeFormatPreference:
        TimeFormatPreference
): String {
    if (!schedule.enabled) {
        return "Schedule disabled"
    }

    return "${formatScheduleClock(schedule.onHour, schedule.onMinute, timeFormatPreference)} – " +
            formatScheduleClock(
                schedule.offHour,
                schedule.offMinute,
                timeFormatPreference
            )
}

internal fun formatScheduleClock(
    hour: Int,
    minute: Int,
    timeFormatPreference:
        TimeFormatPreference
): String {
    val normalizedHour =
        hour.coerceIn(0, 23)

    if (
        timeFormatPreference ==
        TimeFormatPreference
            .TWENTY_FOUR_HOUR
    ) {
        return String.format(
            Locale.US,
            "%02d:%02d",
            normalizedHour,
            minute.coerceIn(0, 59)
        )
    }

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

internal fun formatPhilippineTime(
    value: String,
    timeFormatPreference:
    TimeFormatPreference
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

    val timePattern =
        when (timeFormatPreference) {
            TimeFormatPreference
                .TWELVE_HOUR ->
                "h:mm:ss a"

            TimeFormatPreference
                .TWENTY_FOUR_HOUR ->
                "HH:mm:ss"
        }

    val timeFormatter =
        DateTimeFormatter.ofPattern(
            timePattern,
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

@Composable
private fun RenameOutletDialog(
    relay: RelayNumber,
    currentName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by
    rememberSaveable(relay) {
        mutableStateOf(currentName)
    }

    var errorMessage by
    rememberSaveable(relay) {
        mutableStateOf<String?>(null)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text =
                    "Rename Outlet ${relay.apiValue}"
            )
        },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { newValue ->
                    if (
                        newValue.length <=
                        AppSettings
                            .MAX_FRIENDLY_DEVICE_NAME_LENGTH
                    ) {
                        name = newValue
                        errorMessage = null
                    }
                },
                label = {
                    Text(
                        text = "Outlet name"
                    )
                },
                supportingText = {
                    Text(
                        text =
                            errorMessage
                                ?: "${name.length}/40"
                    )
                },
                isError = errorMessage != null,
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val trimmedName =
                        name.trim()

                    if (trimmedName.isBlank()) {
                        errorMessage =
                            "Enter an outlet name."
                    } else {
                        onSave(trimmedName)
                    }
                }
            ) {
                Text(
                    text = "Save"
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "Cancel"
                )
            }
        }
    )
}

private const val
        RELAY_FEEDBACK_DURATION_MILLISECONDS =
    3_500L
