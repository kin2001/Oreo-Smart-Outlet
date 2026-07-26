package com.iotkin.smartoutlet.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.iotkin.smartoutlet.data.model.DeviceAddress
import com.iotkin.smartoutlet.data.settings.AppSettings
import com.iotkin.smartoutlet.data.settings.AppThemePreference
import com.iotkin.smartoutlet.data.settings.DeviceSettingsStore
import com.iotkin.smartoutlet.data.settings.TimeFormatPreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val appSettings: AppSettings = AppSettings(),
    val savedAddress: DeviceAddress? = null,
    val isSavingFriendlyName: Boolean = false,
    val isResettingSettings: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

private data class SettingsActionState(
    val isSavingFriendlyName: Boolean = false,
    val isResettingSettings: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

class SettingsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val settingsStore =
        DeviceSettingsStore(application)

    private val actionState =
        MutableStateFlow(
            SettingsActionState()
        )

    val uiState: StateFlow<SettingsUiState> =
        combine(
            settingsStore.appSettings,
            settingsStore.savedDeviceAddress,
            actionState
        ) { appSettings, savedAddress, action ->
            SettingsUiState(
                appSettings = appSettings,
                savedAddress = savedAddress,
                isSavingFriendlyName =
                    action.isSavingFriendlyName,
                isResettingSettings =
                    action.isResettingSettings,
                message = action.message,
                error = action.error
            )
        }.stateIn(
            scope = viewModelScope,
            started =
                SharingStarted.WhileSubscribed(
                    stopTimeoutMillis = 5_000
                ),
            initialValue = SettingsUiState()
        )

    fun saveFriendlyDeviceName(
        value: String
    ) {
        val trimmedValue =
            value.trim()

        when {
            trimmedValue.isBlank() -> {
                actionState.update {
                    it.copy(
                        error =
                            "Enter a device name.",
                        message = null
                    )
                }

                return
            }

            trimmedValue.length >
                    AppSettings
                        .MAX_FRIENDLY_DEVICE_NAME_LENGTH -> {
                actionState.update {
                    it.copy(
                        error =
                            "The device name must be 40 characters or fewer.",
                        message = null
                    )
                }

                return
            }
        }

        if (
            actionState.value
                .isSavingFriendlyName
        ) {
            return
        }

        actionState.update {
            it.copy(
                isSavingFriendlyName = true,
                message = null,
                error = null
            )
        }

        viewModelScope.launch {
            runCatching {
                settingsStore
                    .setFriendlyDeviceName(
                        trimmedValue
                    )
            }.onSuccess {
                actionState.update {
                    it.copy(
                        isSavingFriendlyName = false,
                        message =
                            "Device name saved.",
                        error = null
                    )
                }
            }.onFailure {
                actionState.update {
                    it.copy(
                        isSavingFriendlyName = false,
                        message = null,
                        error =
                            "The device name could not be saved."
                    )
                }
            }
        }
    }


    fun setAutomaticDiscoveryEnabled(
        enabled: Boolean
    ) {
        savePreference {
            settingsStore
                .setAutomaticDiscoveryEnabled(
                    enabled
                )
        }
    }

    fun setPollingIntervalSeconds(
        seconds: Int
    ) {
        savePreference {
            settingsStore
                .setPollingIntervalSeconds(
                    seconds
                )
        }
    }

    fun setThemePreference(
        preference: AppThemePreference
    ) {
        savePreference {
            settingsStore
                .setThemePreference(
                    preference
                )
        }
    }

    fun setTimeFormatPreference(
        preference: TimeFormatPreference
    ) {
        savePreference {
            settingsStore
                .setTimeFormatPreference(
                    preference
                )
        }
    }

    fun resetAppSettings() {
        if (
            actionState.value
                .isResettingSettings
        ) {
            return
        }

        actionState.update {
            it.copy(
                isResettingSettings = true,
                message = null,
                error = null
            )
        }

        viewModelScope.launch {
            runCatching {
                settingsStore
                    .resetAppSettings()
            }.onSuccess {
                actionState.update {
                    it.copy(
                        isResettingSettings = false,
                        message =
                            "App settings restored to defaults.",
                        error = null
                    )
                }
            }.onFailure {
                actionState.update {
                    it.copy(
                        isResettingSettings = false,
                        message = null,
                        error =
                            "App settings could not be reset."
                    )
                }
            }
        }
    }

    private fun savePreference(
        updatePreference: suspend () -> Unit
    ) {
        viewModelScope.launch {
            runCatching {
                updatePreference()
            }.onSuccess {
                actionState.update {
                    it.copy(
                        message = null,
                        error = null
                    )
                }
            }.onFailure {
                actionState.update {
                    it.copy(
                        message = null,
                        error =
                            "The setting could not be saved."
                    )
                }
            }
        }
    }

    fun clearFeedback() {
        actionState.update {
            it.copy(
                message = null,
                error = null
            )
        }
    }
}
