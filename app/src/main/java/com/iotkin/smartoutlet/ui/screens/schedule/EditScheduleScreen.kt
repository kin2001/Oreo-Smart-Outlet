package com.iotkin.smartoutlet.ui.screens.schedule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iotkin.smartoutlet.data.model.RelayNumber
import com.iotkin.smartoutlet.data.repository.DeviceConnectionState
import com.iotkin.smartoutlet.ui.screens.diagnostics.DiagnosticsViewModel
import com.iotkin.smartoutlet.ui.theme.OreoShapeTokens
import com.iotkin.smartoutlet.ui.theme.OreoSpacing
import kotlinx.coroutines.delay
import com.iotkin.smartoutlet.data.repository.DeviceStatusRepositoryState
import com.iotkin.smartoutlet.ui.components.writeUnavailableMessage

@Composable
fun EditScheduleRoute(
    relay: RelayNumber,
    viewModel: DiagnosticsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val editorState by viewModel.scheduleEditorState
        .collectAsStateWithLifecycle()

    val statusState by viewModel.statusState
        .collectAsStateWithLifecycle()

    val appSettings by viewModel.appSettings
        .collectAsStateWithLifecycle()

    LaunchedEffect(relay) {
        viewModel.loadScheduleEditor(relay)
    }

    LaunchedEffect(
        editorState.message,
        editorState.error
    ) {
        if (
            editorState.message != null ||
            editorState.error != null
        ) {
            delay(3_500L)
            viewModel.clearScheduleFeedback()
        }
    }

    val deviceAvailable =
        statusState.status != null &&
                statusState.connectionState ==
                DeviceConnectionState.ONLINE &&
                !statusState.isStale

    val currentOutletState =
        when (relay) {
            RelayNumber.RELAY_1 ->
                statusState.status
                    ?.relay1
                    ?.state

            RelayNumber.RELAY_2 ->
                statusState.status
                    ?.relay2
                    ?.state
        }

    EditScheduleScreen(
        relay = relay,
        outletName =
            appSettings
                .outletFriendlyName(relay),
        state = editorState,
        statusState = statusState,
        deviceAvailable = deviceAvailable,
        currentOutletState =
            currentOutletState,
        onEnabledChange =
            viewModel::updateScheduleEnabled,
        onOnHourChange =
            viewModel::updateScheduleOnHour,
        onOnMinuteChange =
            viewModel::updateScheduleOnMinute,
        onOnMeridiemChange =
            viewModel::updateScheduleOnMeridiem,
        onOffHourChange =
            viewModel::updateScheduleOffHour,
        onOffMinuteChange =
            viewModel::updateScheduleOffMinute,
        onOffMeridiemChange =
            viewModel::updateScheduleOffMeridiem,
        onSave = viewModel::saveSchedule,
        onBack = onBack,
        modifier = modifier
    )
}

@Composable
fun EditScheduleScreen(
    relay: RelayNumber,
    outletName: String,
    state: ScheduleEditorUiState,
    statusState:
    DeviceStatusRepositoryState,
    deviceAvailable: Boolean,
    currentOutletState: Boolean?,
    onEnabledChange: (Boolean) -> Unit,
    onOnHourChange: (String) -> Unit,
    onOnMinuteChange: (String) -> Unit,
    onOnMeridiemChange:
        (ScheduleMeridiem) -> Unit,
    onOffHourChange: (String) -> Unit,
    onOffMinuteChange: (String) -> Unit,
    onOffMeridiemChange:
        (ScheduleMeridiem) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = OreoSpacing.ScreenMargin,
            top = OreoSpacing.StackSmall,
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
                onClick = onBack,
                enabled = !state.isSaving
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
                    text =
                        "$outletName Schedule",
                    style =
                        MaterialTheme.typography
                            .headlineLarge,
                    color =
                        MaterialTheme.colorScheme
                            .onBackground
                )

                Text(
                    text =
                        "Set the daily ON and OFF times.",
                    style =
                        MaterialTheme.typography
                            .bodyLarge,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )
            }
        }

        if (!state.isLoaded) {
            item {
                EditorLoadingCard()
            }
        } else {
            item {
                ScheduleEnableCard(
                    enabled = state.enabled,
                    currentOutletState =
                        currentOutletState,
                    controlsEnabled =
                        deviceAvailable &&
                                !state.isSaving,
                    onEnabledChange =
                        onEnabledChange
                )
            }

            item {
                ScheduleTimeEditorCard(
                    title = "Turn outlet on",
                    supportingText =
                        "The outlet turns ON when this time is reached.",
                    time = state.onTime,
                    enabled = !state.isSaving,
                    onHourChange = onOnHourChange,
                    onMinuteChange =
                        onOnMinuteChange,
                    onMeridiemChange =
                        onOnMeridiemChange
                )
            }

            item {
                ScheduleTimeEditorCard(
                    title = "Turn outlet off",
                    supportingText =
                        "The outlet turns OFF when this time is reached.",
                    time = state.offTime,
                    enabled = !state.isSaving,
                    onHourChange = onOffHourChange,
                    onMinuteChange =
                        onOffMinuteChange,
                    onMeridiemChange =
                        onOffMeridiemChange
                )
            }

            state.validationError?.let {
                    message ->
                item {
                    EditorFeedback(
                        message = message,
                        isError = true
                    )
                }
            }

            state.error?.let { message ->
                item {
                    EditorFeedback(
                        message = message,
                        isError = true
                    )
                }
            }

            state.message?.let { message ->
                item {
                    EditorFeedback(
                        message = message,
                        isError = false
                    )
                }
            }

            if (!deviceAvailable) {
                item {
                    EditorFeedback(
                        message =
                            statusState
                                .writeUnavailableMessage(
                                    featureName =
                                        "Schedule saving"
                                )
                                ?: "",
                        isError = true
                    )
                }
            }

            item {
                Button(
                    onClick = onSave,
                    enabled =
                        deviceAvailable &&
                                state.hasUnsavedChanges &&
                                !state.isSaving,
                    modifier =
                        Modifier.fillMaxWidth()
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.padding(
                                end =
                                    OreoSpacing.StackSmall
                            )
                        )
                    }

                    Text(
                        text =
                            if (state.isSaving) {
                                "Saving schedule..."
                            } else {
                                "Save Schedule"
                            }
                    )
                }
            }

            item {
                Text(
                    text =
                        "Saving the schedule does not immediately change the current outlet state. The schedule takes effect at the next matching time.",
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

@Composable
private fun ScheduleEnableCard(
    enabled: Boolean,
    currentOutletState: Boolean?,
    controlsEnabled: Boolean,
    onEnabledChange: (Boolean) -> Unit
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
                        text = "Schedule enabled",
                        style =
                            MaterialTheme.typography
                                .titleLarge,
                        color =
                            MaterialTheme.colorScheme
                                .onSurface
                    )

                    Text(
                        text =
                            if (enabled) {
                                "The saved times will run daily."
                            } else {
                                "The times remain saved but will not run."
                            },
                        style =
                            MaterialTheme.typography
                                .bodyMedium,
                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )
                }

                Switch(
                    checked = enabled,
                    onCheckedChange =
                        onEnabledChange,
                    enabled = controlsEnabled
                )
            }

            Text(
                text =
                    "Current outlet state: " +
                            when (currentOutletState) {
                                true -> "ON"
                                false -> "OFF"
                                null -> "Unavailable"
                            },
                style =
                    MaterialTheme.typography
                        .bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color =
                    MaterialTheme.colorScheme
                        .onSurface
            )
        }
    }
}

@Composable
private fun ScheduleTimeEditorCard(
    title: String,
    supportingText: String,
    time: ScheduleTimeInput,
    enabled: Boolean,
    onHourChange: (String) -> Unit,
    onMinuteChange: (String) -> Unit,
    onMeridiemChange:
        (ScheduleMeridiem) -> Unit
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
            Column(
                verticalArrangement =
                    Arrangement.spacedBy(
                        OreoSpacing.StackSmall
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

                Text(
                    text = supportingText,
                    style =
                        MaterialTheme.typography
                            .bodyMedium,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(
                        OreoSpacing.StackSmall
                    ),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = time.hourText,
                    onValueChange = onHourChange,
                    enabled = enabled,
                    label = {
                        Text(
                            text = "Hour"
                        )
                    },
                    singleLine = true,
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType =
                                KeyboardType.Number
                        ),
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = ":",
                    style =
                        MaterialTheme.typography
                            .headlineMedium
                )

                OutlinedTextField(
                    value = time.minuteText,
                    onValueChange =
                        onMinuteChange,
                    enabled = enabled,
                    label = {
                        Text(
                            text = "Minute"
                        )
                    },
                    singleLine = true,
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType =
                                KeyboardType.Number
                        ),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(
                        OreoSpacing.StackSmall
                    )
            ) {
                FilterChip(
                    selected =
                        time.meridiem ==
                                ScheduleMeridiem.AM,
                    onClick = {
                        onMeridiemChange(
                            ScheduleMeridiem.AM
                        )
                    },
                    enabled = enabled,
                    label = {
                        Text(
                            text = "AM"
                        )
                    },
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected =
                        time.meridiem ==
                                ScheduleMeridiem.PM,
                    onClick = {
                        onMeridiemChange(
                            ScheduleMeridiem.PM
                        )
                    },
                    enabled = enabled,
                    label = {
                        Text(
                            text = "PM"
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun EditorFeedback(
    message: String,
    isError: Boolean
) {
    Text(
        text = message,
        style =
            MaterialTheme.typography
                .bodyMedium,
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
private fun EditorLoadingCard() {
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
                text =
                    "Loading confirmed schedule...",
                style =
                    MaterialTheme.typography
                        .bodyLarge
            )
        }
    }
}
