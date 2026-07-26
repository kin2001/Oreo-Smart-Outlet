package com.iotkin.smartoutlet.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.iotkin.smartoutlet.data.model.DeviceAddress
import com.iotkin.smartoutlet.data.settings.AppSettings
import com.iotkin.smartoutlet.data.settings.AppThemePreference
import com.iotkin.smartoutlet.data.settings.TimeFormatPreference
import com.iotkin.smartoutlet.ui.theme.OreoShapeTokens
import com.iotkin.smartoutlet.ui.theme.OreoSmartOutletTheme
import com.iotkin.smartoutlet.ui.theme.OreoSpacing

private enum class SettingsDialog {
    FRIENDLY_NAME,
    STATUS_REFRESH,
    APPEARANCE,
    TIME_FORMAT,
    RESET
}

@Composable
fun SettingsRoute(
    onManageDevice: () -> Unit,
    onAbout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel()
) {
    val state by viewModel.uiState
        .collectAsStateWithLifecycle()

    SettingsScreen(
        state = state,
        onSaveFriendlyName =
            viewModel::saveFriendlyDeviceName,
        onAutomaticDiscoveryChange =
            viewModel::
            setAutomaticDiscoveryEnabled,
        onPollingIntervalChange =
            viewModel::setPollingIntervalSeconds,
        onThemeChange =
            viewModel::setThemePreference,
        onTimeFormatChange =
            viewModel::setTimeFormatPreference,
        onManageDevice = onManageDevice,
        onResetSettings =
            viewModel::resetAppSettings,
        onAbout = onAbout,
        modifier = modifier
    )
}

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onSaveFriendlyName: (String) -> Unit,
    onAutomaticDiscoveryChange:
        (Boolean) -> Unit,
    onPollingIntervalChange:
        (Int) -> Unit,
    onThemeChange:
        (AppThemePreference) -> Unit,
    onTimeFormatChange:
        (TimeFormatPreference) -> Unit,
    onManageDevice: () -> Unit,
    onResetSettings: () -> Unit,
    onAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var openDialog by rememberSaveable {
        mutableStateOf<SettingsDialog?>(null)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding =
            PaddingValues(
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
            SettingsHeader()
        }

        item {
            SettingsSection(
                title = "Device"
            ) {
                SettingsNavigationRow(
                    title = "Friendly device name",
                    supportingText =
                        state.appSettings
                            .friendlyDeviceName,
                    onClick = {
                        openDialog =
                            SettingsDialog
                                .FRIENDLY_NAME
                    }
                )

                SettingsDivider()

                SettingsValueRow(
                    title = "Saved device address",
                    supportingText =
                        state.savedAddress
                            ?.displayAddress
                            ?: "No device saved"
                )

                SettingsDivider()

                SettingsNavigationRow(
                    title = "Manage Device",
                    supportingText =
                        if (
                            state.savedAddress == null
                        ) {
                            "Connect an outlet or enter its address"
                        } else {
                            "Connection and saved-device options"
                        },
                    onClick = onManageDevice
                )
            }
        }

        item {
            SettingsSection(
                title = "Preferences"
            ) {
                SettingsSwitchRow(
                    title =
                        "Find Outlets Automatically",
                    supportingText =
                        "Search for nearby outlets on your Wi-Fi network.",
                    checked =
                        state.appSettings
                            .automaticDiscoveryEnabled,
                    onCheckedChange =
                        onAutomaticDiscoveryChange
                )

                SettingsDivider()

                SettingsNavigationRow(
                    title = "Status Refresh",
                    supportingText =
                        pollingIntervalLabel(
                            state.appSettings
                                .pollingIntervalSeconds
                        ),
                    onClick = {
                        openDialog =
                            SettingsDialog
                                .STATUS_REFRESH
                    }
                )

                SettingsDivider()

                SettingsNavigationRow(
                    title = "Appearance",
                    supportingText =
                        appearanceLabel(
                            state.appSettings
                                .themePreference
                        ),
                    onClick = {
                        openDialog =
                            SettingsDialog
                                .APPEARANCE
                    }
                )

                SettingsDivider()

                SettingsNavigationRow(
                    title = "Time Format",
                    supportingText =
                        timeFormatLabel(
                            state.appSettings
                                .timeFormatPreference
                        ),
                    onClick = {
                        openDialog =
                            SettingsDialog
                                .TIME_FORMAT
                    }
                )
            }
        }

        item {
            SettingsSection(
                title = "About"
            ) {
                SettingsNavigationRow(
                    title = "About Oreo Smart Outlet",
                    supportingText =
                        "App and firmware version information",
                    onClick = onAbout
                )
            }
        }

        item {
            SettingsSection(
                title = "Reset",
                borderColor =
                    MaterialTheme.colorScheme
                        .error
                        .copy(alpha = 0.25f)
            ) {
                SettingsNavigationRow(
                    title = "Reset App Settings",
                    supportingText =
                        "Restore app preferences without forgetting the saved outlet.",
                    onClick = {
                        openDialog =
                            SettingsDialog.RESET
                    },
                    enabled =
                        !state.isResettingSettings,
                    titleColor =
                        MaterialTheme.colorScheme
                            .error,
                    showChevron = false
                )
            }
        }

        val feedback =
            state.error ?: state.message

        feedback?.let { message ->
            item {
                Text(
                    text = message,
                    style =
                        MaterialTheme.typography
                            .bodyMedium,
                    color =
                        if (state.error != null) {
                            MaterialTheme
                                .colorScheme.error
                        } else {
                            MaterialTheme
                                .colorScheme.primary
                        },
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    when (openDialog) {
        SettingsDialog.FRIENDLY_NAME ->
            FriendlyDeviceNameDialog(
                currentName =
                    state.appSettings
                        .friendlyDeviceName,
                isSaving =
                    state.isSavingFriendlyName,
                onDismiss = {
                    openDialog = null
                },
                onSave = { name ->
                    openDialog = null
                    onSaveFriendlyName(name)
                }
            )

        SettingsDialog.STATUS_REFRESH ->
            SingleChoiceDialog(
                title = "Status Refresh",
                options =
                    listOf(
                        2 to "Every 2 seconds",
                        5 to "Every 5 seconds",
                        10 to "Every 10 seconds",
                        30 to "Every 30 seconds"
                    ),
                selected =
                    state.appSettings
                        .pollingIntervalSeconds,
                onDismiss = {
                    openDialog = null
                },
                onSelected = { seconds ->
                    openDialog = null
                    onPollingIntervalChange(
                        seconds
                    )
                }
            )

        SettingsDialog.APPEARANCE ->
            SingleChoiceDialog(
                title = "Appearance",
                options =
                    listOf(
                        AppThemePreference.SYSTEM to
                                "Use Device Setting",
                        AppThemePreference.LIGHT to
                                "Light",
                        AppThemePreference.DARK to
                                "Dark"
                    ),
                selected =
                    state.appSettings
                        .themePreference,
                onDismiss = {
                    openDialog = null
                },
                onSelected = { preference ->
                    openDialog = null
                    onThemeChange(preference)
                }
            )

        SettingsDialog.TIME_FORMAT ->
            SingleChoiceDialog(
                title = "Time Format",
                options =
                    listOf(
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
                onDismiss = {
                    openDialog = null
                },
                onSelected = { preference ->
                    openDialog = null
                    onTimeFormatChange(
                        preference
                    )
                }
            )

        SettingsDialog.RESET ->
            AlertDialog(
                onDismissRequest = {
                    if (
                        !state.isResettingSettings
                    ) {
                        openDialog = null
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
                            "Appearance, time format, discovery, status refresh, device name, and outlet names will return to their defaults. The saved device will remain connected."
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            openDialog = null
                            onResetSettings()
                        },
                        enabled =
                            !state
                                .isResettingSettings
                    ) {
                        Text(text = "Reset")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            openDialog = null
                        },
                        enabled =
                            !state
                                .isResettingSettings
                    ) {
                        Text(text = "Cancel")
                    }
                }
            )

        null -> Unit
    }
}

@Composable
private fun SettingsHeader() {
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

@Composable
private fun SettingsSection(
    title: String,
    borderColor: Color =
        MaterialTheme.colorScheme
            .outlineVariant
            .copy(alpha = 0.35f),
    content:
        @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OreoShapeTokens.ExtraLarge,
        color =
            MaterialTheme.colorScheme
                .surfaceContainerLowest,
        border =
            BorderStroke(
                width = 1.dp,
                color = borderColor
            )
    ) {
        Column(
            modifier =
                Modifier.padding(
                    horizontal =
                        OreoSpacing.CardPadding,
                    vertical =
                        OreoSpacing.StackMedium
                )
        ) {
            Text(
                text = title,
                style =
                    MaterialTheme.typography
                        .headlineMedium,
                modifier =
                    Modifier.padding(
                        bottom =
                            OreoSpacing.StackSmall
                    )
            )

            content()
        }
    }
}

@Composable
private fun SettingsNavigationRow(
    title: String,
    supportingText: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    titleColor: Color =
        MaterialTheme.colorScheme.onSurface,
    showChevron: Boolean = true
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .defaultMinSize(
                        minHeight = 48.dp
                    )
                    .padding(
                        vertical =
                            OreoSpacing.StackSmall
                    ),
            horizontalArrangement =
                Arrangement.spacedBy(
                    OreoSpacing.StackMedium
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            SettingsRowText(
                title = title,
                supportingText =
                    supportingText,
                titleColor = titleColor,
                modifier = Modifier.weight(1f)
            )

            if (showChevron) {
                Icon(
                    imageVector =
                        Icons.Filled
                            .KeyboardArrowRight,
                    contentDescription = null,
                    tint =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SettingsValueRow(
    title: String,
    supportingText: String
) {
    SettingsRowText(
        title = title,
        supportingText = supportingText,
        modifier =
            Modifier
                .fillMaxWidth()
                .defaultMinSize(
                    minHeight = 48.dp
                )
                .padding(
                    vertical =
                        OreoSpacing.StackSmall
                )
    )
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    supportingText: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .toggleable(
                    value = checked,
                    role = Role.Switch,
                    onValueChange =
                        onCheckedChange
                )
                .defaultMinSize(
                    minHeight = 48.dp
                )
                .padding(
                    vertical =
                        OreoSpacing.StackSmall
                ),
        horizontalArrangement =
            Arrangement.spacedBy(
                OreoSpacing.StackMedium
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        SettingsRowText(
            title = title,
            supportingText = supportingText,
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = checked,
            onCheckedChange = null
        )
    }
}

@Composable
private fun SettingsRowText(
    title: String,
    supportingText: String,
    modifier: Modifier = Modifier,
    titleColor: Color =
        MaterialTheme.colorScheme.onSurface
) {
    Column(
        modifier = modifier,
        verticalArrangement =
            Arrangement.spacedBy(
                OreoSpacing.Base
            )
    ) {
        Text(
            text = title,
            style =
                MaterialTheme.typography
                    .bodyLarge,
            color = titleColor
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
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        color =
            MaterialTheme.colorScheme
                .outlineVariant
                .copy(alpha = 0.35f)
    )
}

@Composable
private fun FriendlyDeviceNameDialog(
    currentName: String,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by rememberSaveable(currentName) {
        mutableStateOf(currentName)
    }

    val trimmedName = name.trim()

    val validationMessage =
        when {
            trimmedName.isBlank() ->
                "Enter a device name."

            trimmedName.length >
                    AppSettings
                        .MAX_FRIENDLY_DEVICE_NAME_LENGTH ->
                "Use 40 characters or fewer."

            else -> null
        }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Friendly device name"
            )
        },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                },
                label = {
                    Text(text = "Device name")
                },
                supportingText = {
                    Text(
                        text =
                            validationMessage
                                ?: "${name.length}/40"
                    )
                },
                isError =
                    validationMessage != null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(trimmedName)
                },
                enabled =
                    !isSaving &&
                            validationMessage == null
            ) {
                Text(
                    text =
                        if (isSaving) {
                            "Saving..."
                        } else {
                            "Save"
                        }
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSaving
            ) {
                Text(text = "Cancel")
            }
        }
    )
}

@Composable
private fun <T> SingleChoiceDialog(
    title: String,
    options: List<Pair<T, String>>,
    selected: T,
    onDismiss: () -> Unit,
    onSelected: (T) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title)
        },
        text = {
            Column {
                options.forEach { option ->
                    val isSelected =
                        option.first == selected

                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected =
                                        isSelected,
                                    role =
                                        Role.RadioButton,
                                    onClick = {
                                        onSelected(
                                            option.first
                                        )
                                    }
                                )
                                .defaultMinSize(
                                    minHeight = 48.dp
                                )
                                .padding(
                                    vertical =
                                        OreoSpacing.Base
                                ),
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = null
                        )

                        Text(
                            text = option.second,
                            style =
                                MaterialTheme
                                    .typography
                                    .bodyLarge,
                            modifier =
                                Modifier.padding(
                                    start =
                                        OreoSpacing
                                            .StackSmall
                                )
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(text = "Cancel")
            }
        }
    )
}

internal fun pollingIntervalLabel(
    seconds: Int
): String {
    return "Every $seconds seconds"
}

internal fun appearanceLabel(
    preference: AppThemePreference
): String {
    return when (preference) {
        AppThemePreference.SYSTEM ->
            "Use Device Setting"

        AppThemePreference.LIGHT ->
            "Light"

        AppThemePreference.DARK ->
            "Dark"
    }
}

internal fun timeFormatLabel(
    preference: TimeFormatPreference
): String {
    return when (preference) {
        TimeFormatPreference.TWELVE_HOUR ->
            "12-hour"

        TimeFormatPreference
            .TWENTY_FOUR_HOUR ->
            "24-hour"
    }
}

@Preview(
    name = "Modern Settings Light",
    widthDp = 360,
    showBackground = true
)
@Composable
private fun ModernSettingsLightPreview() {
    OreoSmartOutletTheme(
        darkTheme = false
    ) {
        SettingsScreenPreviewContent()
    }
}

@Preview(
    name = "Modern Settings Large Font",
    widthDp = 360,
    fontScale = 2f,
    showBackground = true,
    backgroundColor = 0xFF0F172A
)
@Composable
private fun ModernSettingsLargeFontPreview() {
    OreoSmartOutletTheme(
        darkTheme = true
    ) {
        SettingsScreenPreviewContent()
    }
}

@Composable
private fun SettingsScreenPreviewContent() {
    Surface(
        color =
            MaterialTheme.colorScheme
                .background
    ) {
        SettingsScreen(
            state =
                SettingsUiState(
                    savedAddress =
                        DeviceAddress(
                            host =
                                "192.168.8.113",
                            port = 8080
                        )
                ),
            onSaveFriendlyName = {},
            onAutomaticDiscoveryChange = {},
            onPollingIntervalChange = {},
            onThemeChange = {},
            onTimeFormatChange = {},
            onManageDevice = {},
            onResetSettings = {},
            onAbout = {}
        )
    }
}
