package com.iotkin.smartoutlet.ui.screens.diagnostics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.iotkin.smartoutlet.data.model.RelayNumber
import com.iotkin.smartoutlet.data.network.DeviceActionResult
import com.iotkin.smartoutlet.data.network.SmartOutletNetworkFactory
import com.iotkin.smartoutlet.data.repository.DeviceConnectionState
import com.iotkin.smartoutlet.data.repository.DeviceStatusRepositoryState
import com.iotkin.smartoutlet.data.repository.RelayCommandResult
import com.iotkin.smartoutlet.data.repository.SmartOutletRepository
import com.iotkin.smartoutlet.data.repository.StatusRefreshOutcome
import com.iotkin.smartoutlet.data.repository.StatusRefreshReason
import com.iotkin.smartoutlet.data.settings.DeviceSettingsStore
import com.iotkin.smartoutlet.ui.screens.home.RelayControlUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    /**
     * Shared live device status.
     *
     * Home, Relay Details, and Diagnostics will read the
     * same repository state and polling loop.
     */
    val statusState:
            StateFlow<DeviceStatusRepositoryState> =
        repository.statusState

    private val actionState =
        MutableStateFlow(
            DiagnosticsActionState()
        )

    private val _relayControlState =
        MutableStateFlow(
            RelayControlUiState()
        )

    val relayControlState:
            StateFlow<RelayControlUiState> =
        _relayControlState.asStateFlow()

    val uiState: StateFlow<DiagnosticsUiState> =
        combine(
            repository.statusState,
            actionState
        ) { repositoryState, currentActionState ->
            DiagnosticsUiState(
                repositoryState = repositoryState,
                isRunningStatusAction =
                    currentActionState
                        .isRunningStatusAction,
                isRequestingTimeSync =
                    currentActionState
                        .isRequestingTimeSync,
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
                .isRequestingTimeSync ||
            _relayControlState.value
                .isAnyRelayUpdating
        ) {
            return
        }

        actionState.update { currentState ->
            currentState.copy(
                isRequestingTimeSync = true,
                message = null,
                error = null
            )
        }

        viewModelScope.launch {
            val result =
                repository.requestTimeSync()

            actionState.update { currentState ->
                when (result) {
                    DeviceActionResult.Success -> {
                        currentState.copy(
                            isRequestingTimeSync = false,
                            message =
                                "Philippine time synchronization requested.",
                            error = null
                        )
                    }

                    DeviceActionResult.Timeout -> {
                        currentState.copy(
                            isRequestingTimeSync = false,
                            message = null,
                            error =
                                "The time synchronization request timed out."
                        )
                    }

                    DeviceActionResult.NoSavedDevice -> {
                        currentState.copy(
                            isRequestingTimeSync = false,
                            message = null,
                            error =
                                "No smart outlet address is saved."
                        )
                    }

                    DeviceActionResult
                        .SkippedAlreadyRunning -> {
                        currentState.copy(
                            isRequestingTimeSync = false,
                            message = null,
                            error =
                                "Another device action is already running."
                        )
                    }

                    is DeviceActionResult.HttpError -> {
                        currentState.copy(
                            isRequestingTimeSync = false,
                            message = null,
                            error = result.message
                        )
                    }

                    is DeviceActionResult
                    .InvalidResponse -> {
                        currentState.copy(
                            isRequestingTimeSync = false,
                            message = null,
                            error = result.message
                        )
                    }

                    is DeviceActionResult.NetworkError -> {
                        currentState.copy(
                            isRequestingTimeSync = false,
                            message = null,
                            error = result.message
                        )
                    }
                }
            }
        }
    }

    fun setRelayState(
        relay: RelayNumber,
        desiredState: Boolean
    ) {
        val currentRelayState =
            _relayControlState.value

        if (
            currentRelayState
                .isAnyRelayUpdating ||
            actionState.value
                .isRequestingTimeSync
        ) {
            return
        }

        val currentStatus =
            repository.statusState.value

        if (
            currentStatus.connectionState !=
            DeviceConnectionState.ONLINE ||
            currentStatus.isStale
        ) {
            _relayControlState.update {
                it.copy(
                    updatingRelay = null,
                    message = null,
                    error =
                        "Outlet controls are unavailable while the device is offline or reconnecting."
                )
            }

            return
        }

        _relayControlState.update {
            RelayControlUiState(
                updatingRelay = relay
            )
        }

        viewModelScope.launch {
            val result =
                repository.setRelayState(
                    relay = relay,
                    desiredState = desiredState
                )

            _relayControlState.update {
                when (result) {
                    is RelayCommandResult.Success -> {
                        RelayControlUiState(
                            message =
                                relaySuccessMessage(
                                    relay =
                                        result.relay,
                                    state =
                                        result.confirmedState
                                )
                        )
                    }

                    is RelayCommandResult
                    .ConfirmedButRefreshFailed -> {
                        RelayControlUiState(
                            error =
                                result.message
                        )
                    }

                    RelayCommandResult.NoSavedDevice -> {
                        RelayControlUiState(
                            error =
                                "No smart outlet address is saved."
                        )
                    }

                    RelayCommandResult.DeviceUnavailable -> {
                        RelayControlUiState(
                            error =
                                "Relay controls are unavailable while the device is offline or reconnecting."
                        )
                    }

                    RelayCommandResult
                        .SkippedAlreadyRunning -> {
                        RelayControlUiState(
                            error =
                                "Another device action is already running."
                        )
                    }

                    is RelayCommandResult.Failed -> {
                        RelayControlUiState(
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

    fun clearRelayFeedback() {
        _relayControlState.update {
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
        if (
            actionState.value
                .isRunningStatusAction
        ) {
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

    private fun relaySuccessMessage(
        relay: RelayNumber,
        state: Boolean
    ): String {
        val stateText =
            if (state) {
                "ON"
            } else {
                "OFF"
            }

        return "Outlet ${relay.apiValue} is now $stateText."
    }
}