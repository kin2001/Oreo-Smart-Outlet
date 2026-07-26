package com.iotkin.smartoutlet.ui.screens.settings

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.iotkin.smartoutlet.data.settings.AppThemePreference
import com.iotkin.smartoutlet.data.settings.TimeFormatPreference
import com.iotkin.smartoutlet.ui.theme.OreoShapeTokens
import com.iotkin.smartoutlet.ui.theme.OreoSpacing


@Composable
fun SettingsRoute(
    onManageDevice: () -> Unit,
    onDeviceRemoved: () -> Unit,
    onAbout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel()
) {
    val state by viewModel.uiState
        .collectAsStateWithLifecycle()

    var friendlyName by rememberSaveable {
        mutableStateOf(
            state.appSettings
                .friendlyDeviceName
        )
    }

    var showRemoveConfirmation by
    rememberSaveable {
        mutableStateOf(false)
    }

    var showResetConfirmation by
    rememberSaveable {
        mutableStateOf(false)
    }

    LaunchedEffect(
        state.appSettings
            .friendlyDeviceName
    ) {
        friendlyName =
            state.appSettings
                .friendlyDeviceName
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                SettingsEvent
                    .SavedDeviceRemoved -> {
                    onDeviceRemoved()
                }
            }
        }
    }

    SettingsScreen(
        state = state,
        friendlyName = friendlyName,
        onFriendlyNameChange = {
            friendlyName = it
            viewModel.clearFeedback()
        },
        onSaveFriendlyName = {
            viewModel
                .saveFriendlyDeviceName(
                    friendlyName
                )
        },
        onAutomaticDiscoveryChange =
            viewModel::
            setAutomaticDiscoveryEnabled,
        onPollingIntervalChange =
            viewModel::
            setPollingIntervalSeconds,
        onThemeChange =
            viewModel::setThemePreference,
        onTimeFormatChange =
            viewModel::
            setTimeFormatPreference,
        onManageDevice = onManageDevice,
        onRemoveDevice = {
            showRemoveConfirmation = true
        },
        onResetSettings = {
            showResetConfirmation = true
        },
        onAbout = onAbout,
        modifier = modifier
    )

    if (showRemoveConfirmation) {
        AlertDialog(
            onDismissRequest = {
                if (!state.isRemovingDevice) {
                    showRemoveConfirmation =
                        false
                }
            },
            title = {
                Text(
                    text =
                        "Remove saved device?"
                )
            },
            text = {
                Text(
                    text =
                        "The saved IP address and port will be removed. You will need to connect to the outlet again."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRemoveConfirmation =
                            false

                        viewModel
                            .removeSavedDevice()
                    },
                    enabled =
                        !state.isRemovingDevice
                ) {
                    Text(
                        text = "Remove"
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRemoveConfirmation =
                            false
                    },
                    enabled =
                        !state.isRemovingDevice
                ) {
                    Text(
                        text = "Cancel"
                    )
                }
            }
        )
    }

    if (showResetConfirmation) {
        AlertDialog(
            onDismissRequest = {
                if (
                    !state.isResettingSettings
                ) {
                    showResetConfirmation =
                        false
                }
            },
            title = {
                Text(
                    text =
                        "Reset app settings?"
                )
            },
            text = {
                Text(
                    text =
                        "Theme, time format, discovery, polling interval, and friendly name will return to their defaults. The saved device will remain connected."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetConfirmation =
                            false

                        viewModel
                            .resetAppSettings()
                    },
                    enabled =
                        !state.isResettingSettings
                ) {
                    Text(
                        text = "Reset"
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showResetConfirmation =
                            false
                    },
                    enabled =
                        !state.isResettingSettings
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
fun SettingsScreen(
    state: SettingsUiState,
    friendlyName: String,
    onFriendlyNameChange: (String) -> Unit,
    onSaveFriendlyName: () -> Unit,
    onAutomaticDiscoveryChange:
        (Boolean) -> Unit,
    onPollingIntervalChange:
        (Int) -> Unit,
    onThemeChange:
        (AppThemePreference) -> Unit,
    onTimeFormatChange:
        (TimeFormatPreference) -> Unit,
    onManageDevice: () -> Unit,
    onRemoveDevice: () -> Unit,
    onResetSettings: () -> Unit,
    onAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = OreoSpacing.ScreenMargin,
            top = OreoSpacing.StackMedium,
            end = OreoSpacing.ScreenMargin,
            bottom = OreoSpacing.StackLarge
        ),
        verticalArrangement =
            Arrangement.spacedBy(
                OreoSpacing.StackMedium
            )
    ) {
        item {
            Column(
                verticalArrangement =
                    Arrangement.spacedBy(
                        OreoSpacing.StackSmall
                    )
            ) {
                Text(
                    text = "Settings",
                    style =
                        MaterialTheme.typography
                            .headlineLarge,
                    color =
                        MaterialTheme.colorScheme
                            .onBackground
                )

                Text(
                    text =
                        "Manage your outlet and app preferences.",
                    style =
                        MaterialTheme.typography
                            .bodyLarge,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )
            }
        }

        item {
            Surface(
                modifier =
                    Modifier.fillMaxWidth(),
                shape =
                    OreoShapeTokens.ExtraLarge,
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
                        text = "Device",
                        style =
                            MaterialTheme.typography
                                .headlineMedium
                    )

                    OutlinedTextField(
                        value = friendlyName,
                        onValueChange =
                            onFriendlyNameChange,
                        modifier =
                            Modifier.fillMaxWidth(),
                        label = {
                            Text(
                                text =
                                    "Friendly device name"
                            )
                        },
                        supportingText = {
                            Text(
                                text =
                                    "${friendlyName.length}/40"
                            )
                        },
                        singleLine = true
                    )

                    Button(
                        onClick =
                            onSaveFriendlyName,
                        enabled =
                            !state
                                .isSavingFriendlyName,
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text =
                                if (
                                    state
                                        .isSavingFriendlyName
                                ) {
                                    "Saving name..."
                                } else {
                                    "Save Device Name"
                                }
                        )
                    }

                    Text(
                        text =
                            state.savedAddress
                                ?.let { address ->
                                    "${address.host}:${address.port}"
                                }
                                ?: "No device is saved.",
                        style =
                            MaterialTheme.typography
                                .bodyMedium,
                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )

                    OutlinedButton(
                        onClick =
                            onManageDevice,
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text =
                                if (
                                    state.savedAddress ==
                                    null
                                ) {
                                    "Connect Device"
                                } else {
                                    "Manage Device"
                                }
                        )
                    }

                    if (
                        state.savedAddress != null
                    ) {
                        OutlinedButton(
                            onClick =
                                onRemoveDevice,
                            enabled =
                                !state
                                    .isRemovingDevice,
                            colors =
                                ButtonDefaults
                                    .outlinedButtonColors(
                                        contentColor =
                                            MaterialTheme
                                                .colorScheme
                                                .error
                                    ),
                            modifier =
                                Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text =
                                    if (
                                        state
                                            .isRemovingDevice
                                    ) {
                                        "Removing device..."
                                    } else {
                                        "Remove Saved Device"
                                    }
                            )
                        }
                    }
                }
            }
        }

        item {
            Surface(
                modifier =
                    Modifier.fillMaxWidth(),
                shape =
                    OreoShapeTokens.ExtraLarge,
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
                        text = "App preferences",
                        style =
                            MaterialTheme.typography
                                .headlineMedium
                    )

                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.spacedBy(
                                OreoSpacing.StackMedium
                            ),
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {
                        Column(
                            modifier =
                                Modifier.weight(1f),
                            verticalArrangement =
                                Arrangement.spacedBy(
                                    OreoSpacing.Base
                                )
                        ) {
                            Text(
                                text =
                                    "Automatic discovery",
                                style =
                                    MaterialTheme
                                        .typography
                                        .bodyLarge
                            )

                            Text(
                                text =
                                    "Search for the outlet automatically on your Wi-Fi network.",
                                style =
                                    MaterialTheme
                                        .typography
                                        .bodyMedium,
                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .onSurfaceVariant
                            )
                        }

                        Switch(
                            checked =
                                state.appSettings
                                    .automaticDiscoveryEnabled,
                            onCheckedChange =
                                onAutomaticDiscoveryChange
                        )
                    }

                    HorizontalDivider(
                        color =
                            MaterialTheme.colorScheme
                                .outlineVariant
                                .copy(alpha = 0.35f)
                    )

                    PreferenceChoice(
                        title = "Polling interval",
                        description =
                            "How often the app requests the latest outlet status.",
                        options = listOf(
                            2 to "2 s",
                            5 to "5 s",
                            10 to "10 s",
                            30 to "30 s"
                        ),
                        selected =
                            state.appSettings
                                .pollingIntervalSeconds,
                        onSelected =
                            onPollingIntervalChange
                    )

                    HorizontalDivider(
                        color =
                            MaterialTheme.colorScheme
                                .outlineVariant
                                .copy(alpha = 0.35f)
                    )

                    PreferenceChoice(
                        title = "Theme",
                        description =
                            "Choose how the app selects its colors.",
                        options = listOf(
                            AppThemePreference.SYSTEM to
                                    "System",
                            AppThemePreference.LIGHT to
                                    "Light",
                            AppThemePreference.DARK to
                                    "Dark"
                        ),
                        selected =
                            state.appSettings
                                .themePreference,
                        onSelected = onThemeChange
                    )

                    HorizontalDivider(
                        color =
                            MaterialTheme.colorScheme
                                .outlineVariant
                                .copy(alpha = 0.35f)
                    )

                    PreferenceChoice(
                        title = "Time format",
                        description =
                            "Choose how times are displayed in the app.",
                        options = listOf(
                            TimeFormatPreference
                                .TWELVE_HOUR to
                                    "12-hour",
                            TimeFormatPreference
                                .TWENTY_FOUR_HOUR to
                                    "24-hour"
                        ),
                        selected =
                            state.appSettings
                                .timeFormatPreference,
                        onSelected =
                            onTimeFormatChange
                    )
                }
            }
        }

        item {
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
                        text = "About",
                        style =
                            MaterialTheme.typography
                                .headlineMedium
                    )

                    Text(
                        text =
                            "View the app version and the firmware version reported by your outlet.",
                        style =
                            MaterialTheme.typography
                                .bodyMedium,
                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )

                    OutlinedButton(
                        onClick = onAbout,
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Open About")
                    }
                }
            }
        }

        item {
            Surface(
                modifier =
                    Modifier.fillMaxWidth(),
                shape =
                    OreoShapeTokens.ExtraLarge,
                color =
                    MaterialTheme.colorScheme
                        .surfaceContainerLowest,
                border = BorderStroke(
                    width = 1.dp,
                    color =
                        MaterialTheme.colorScheme
                            .error
                            .copy(alpha = 0.25f)
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
                        text = "Reset",
                        style =
                            MaterialTheme.typography
                                .headlineMedium
                    )

                    Text(
                        text =
                            "Restore app preferences to their defaults without removing the saved outlet.",
                        style =
                            MaterialTheme.typography
                                .bodyMedium,
                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )

                    OutlinedButton(
                        onClick =
                            onResetSettings,
                        enabled =
                            !state
                                .isResettingSettings,
                        colors =
                            ButtonDefaults
                                .outlinedButtonColors(
                                    contentColor =
                                        MaterialTheme
                                            .colorScheme
                                            .error
                                ),
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text =
                                if (
                                    state
                                        .isResettingSettings
                                ) {
                                    "Resetting..."
                                } else {
                                    "Reset App Settings"
                                }
                        )
                    }
                }
            }
        }

        val feedback =
            state.error
                ?: state.message

        feedback?.let { message ->
            item {
                Text(
                    text = message,
                    style =
                        MaterialTheme.typography
                            .bodyMedium,
                    color =
                        if (
                            state.error != null
                        ) {
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
private fun <T> PreferenceChoice(
    title: String,
    description: String,
    options: List<Pair<T, String>>,
    selected: T,
    onSelected: (T) -> Unit
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
                    .bodyLarge
        )

        Text(
            text = description,
            style =
                MaterialTheme.typography
                    .bodyMedium,
            color =
                MaterialTheme.colorScheme
                    .onSurfaceVariant
        )

        SingleChoiceSegmentedButtonRow(
            modifier =
                Modifier.fillMaxWidth()
        ) {
            options.forEachIndexed {
                    index,
                    option ->

                SegmentedButton(
                    selected =
                        selected ==
                                option.first,
                    onClick = {
                        onSelected(
                            option.first
                        )
                    },
                    shape =
                        SegmentedButtonDefaults
                            .itemShape(
                                index = index,
                                count =
                                    options.size
                            ),
                    label = {
                        Text(
                            text =
                                option.second
                        )
                    }
                )
            }
        }
    }
}
