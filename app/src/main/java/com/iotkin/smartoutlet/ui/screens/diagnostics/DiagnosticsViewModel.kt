package com.iotkin.smartoutlet.ui.screens.diagnostics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.iotkin.smartoutlet.data.network.DeviceActionResult
import com.iotkin.smartoutlet.data.network.SmartOutletNetworkFactory
import com.iotkin.smartoutlet.data.repository.DeviceStatusRepositoryState
import com.iotkin.smartoutlet.data.repository.SmartOutletRepository
import com.iotkin.smartoutlet.data.repository.StatusRefreshOutcome
import com.iotkin.smartoutlet.data.repository.StatusRefreshReason
import com.iotkin.smartoutlet.data.settings.DeviceSettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DiagnosticsUiState(
    val repositoryState: DeviceStatusRepositoryState =
        DeviceStatusRepositoryState(),
    val isRunningStatusAction: Boolean = false,
    val isRequestingTimeSync: Boolean = false,
    val actionMessage: String? = null,
    val actionError: String? = null
)

private data class DiagnosticsActionState(
    val isRunningStatusAction: Boolean = false,
    val isRequestingTimeSync: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

class DiagnosticsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository =
        SmartOutletRepository(
            settingsStore =
                DeviceSettingsStore(application),
            networkFactory =
                SmartOutletNetworkFactory(),
            scope = viewModelScope
        )

    private val actionState =
        MutableStateFlow(
            DiagnosticsActionState()
        )

    val uiState: StateFlow<DiagnosticsUiState> =
        combine(
            repository.statusState,
            actionState
        ) { repositoryState, currentActionState ->
            DiagnosticsUiState(
                repositoryState = repositoryState,
                isRunningStatusAction =
                    currentActionState.isRunningStatusAction,
                isRequestingTimeSync =
                    currentActionState.isRequestingTimeSync,
                actionMessage =
                    currentActionState.message,
                actionError =
                    currentActionState.error
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(
                stopTimeoutMillis = 5_000
            ),
            initialValue = DiagnosticsUiState()
        )

    fun startForegroundPolling() {
        repository.startForegroundPolling()
    }

    fun stopForegroundPolling() {
        repository.stopForegroundPolling()
    }

    fun refreshDeviceStatus() {
        runStatusAction(
            reason =
                StatusRefreshReason.MANUAL_REFRESH,
            successMessage =
                "Device status refreshed."
        )
    }

    fun testConnection() {
        runStatusAction(
            reason =
                StatusRefreshReason.TEST_CONNECTION,
            successMessage =
                "Connection test passed."
        )
    }

    fun requestTimeSync() {
        if (
            actionState.value
                .isRequestingTimeSync
        ) {
            return
        }

        actionState.update {
            DiagnosticsActionState(
                isRequestingTimeSync = true
            )
        }

        viewModelScope.launch {
            val result =
                repository.requestTimeSync()

            actionState.update {
                when (result) {
                    DeviceActionResult.Success -> {
                        DiagnosticsActionState(
                            message =
                                "Philippine time synchronization requested."
                        )
                    }

                    DeviceActionResult.Timeout -> {
                        DiagnosticsActionState(
                            error =
                                "The time synchronization request timed out."
                        )
                    }

                    DeviceActionResult.NoSavedDevice -> {
                        DiagnosticsActionState(
                            error =
                                "No smart outlet address is saved."
                        )
                    }

                    DeviceActionResult
                        .SkippedAlreadyRunning -> {
                        DiagnosticsActionState(
                            error =
                                "Another device action is already running."
                        )
                    }

                    is DeviceActionResult.HttpError -> {
                        DiagnosticsActionState(
                            error = result.message
                        )
                    }

                    is DeviceActionResult
                    .InvalidResponse -> {
                        DiagnosticsActionState(
                            error = result.message
                        )
                    }

                    is DeviceActionResult.NetworkError -> {
                        DiagnosticsActionState(
                            error = result.message
                        )
                    }
                }
            }
        }
    }

    fun clearActionFeedback() {
        actionState.update {
            it.copy(
                message = null,
                error = null
            )
        }
    }

    private fun runStatusAction(
        reason: StatusRefreshReason,
        successMessage: String
    ) {
        if (actionState.value.isRunningStatusAction) {
            return
        }

        actionState.update { currentState ->
            currentState.copy(
                isRunningStatusAction = true,
                message = null,
                error = null
            )
        }

        viewModelScope.launch {
            val outcome =
                repository.refreshStatus(
                    reason = reason
                )

            actionState.update { currentState ->
                when (outcome) {
                    StatusRefreshOutcome.SUCCESS -> {
                        currentState.copy(
                            isRunningStatusAction = false,
                            message = successMessage,
                            error = null
                        )
                    }

                    StatusRefreshOutcome.FAILED -> {
                        currentState.copy(
                            isRunningStatusAction = false,
                            message = null,
                            error =
                                repository.statusState
                                    .value
                                    .errorMessage
                                    ?: "The status request failed."
                        )
                    }

                    StatusRefreshOutcome.NO_SAVED_DEVICE -> {
                        currentState.copy(
                            isRunningStatusAction = false,
                            message = null,
                            error =
                                "No smart outlet address is saved."
                        )
                    }

                    StatusRefreshOutcome
                        .SKIPPED_ALREADY_RUNNING -> {
                        currentState.copy(
                            isRunningStatusAction = false
                        )
                    }
                }
            }
        }
    }
}