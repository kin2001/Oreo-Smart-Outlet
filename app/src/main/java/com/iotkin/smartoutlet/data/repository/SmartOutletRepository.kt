package com.iotkin.smartoutlet.data.repository

import com.iotkin.smartoutlet.data.model.DeviceAddress
import com.iotkin.smartoutlet.data.network.DeviceStatusResult
import com.iotkin.smartoutlet.data.network.SmartOutletNetworkFactory
import com.iotkin.smartoutlet.data.settings.DeviceSettingsStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import com.iotkin.smartoutlet.data.network.DeviceActionResult

class SmartOutletRepository(
    private val settingsStore: DeviceSettingsStore,
    private val networkFactory: SmartOutletNetworkFactory,
    private val scope: CoroutineScope,
    private val currentTimeMillis: () -> Long = {
        System.currentTimeMillis()
    }
) {
    private val _statusState =
        MutableStateFlow(
            DeviceStatusRepositoryState()
        )

    val statusState: StateFlow<DeviceStatusRepositoryState> =
        _statusState.asStateFlow()

    private val statusRequestMutex = Mutex()
    private val writeRequestMutex = Mutex()

    private var pollingJob: Job? = null

    init {
        observeSavedAddress()
    }

    fun startForegroundPolling() {
        if (pollingJob?.isActive == true) {
            return
        }

        pollingJob = scope.launch {
            while (isActive) {
                refreshStatus(
                    reason =
                        if (
                            _statusState.value
                                .lastSuccessfulRefreshEpochMillis ==
                            null
                        ) {
                            StatusRefreshReason.FOREGROUND_START
                        } else {
                            StatusRefreshReason.POLLING
                        }
                )

                delay(
                    pollingDelayMillis(
                        failureCount =
                            _statusState.value
                                .consecutiveFailures
                    )
                )
            }
        }
    }

    fun stopForegroundPolling() {
        pollingJob?.cancel()
        pollingJob = null

        _statusState.update { currentState ->
            currentState.copy(
                isRefreshing = false
            )
        }
    }

    suspend fun requestTimeSync(): DeviceActionResult {
        if (!writeRequestMutex.tryLock()) {
            return DeviceActionResult.SkippedAlreadyRunning
        }

        try {
            val address = resolveSavedAddress()
                ?: return DeviceActionResult.NoSavedDevice

            val result = networkFactory
                .createClient(address)
                .requestTimeSync()

            if (result is DeviceActionResult.Success) {
                refreshStatus(
                    reason = StatusRefreshReason.AFTER_ACTION
                )
            }

            return result
        } finally {
            writeRequestMutex.unlock()
        }
    }

    suspend fun refreshStatus(
        reason: StatusRefreshReason =
            StatusRefreshReason.MANUAL_REFRESH
    ): StatusRefreshOutcome {
        val lockAcquired =
            when (reason) {
                StatusRefreshReason.POLLING,
                StatusRefreshReason.FOREGROUND_START -> {
                    statusRequestMutex.tryLock()
                }

                StatusRefreshReason.MANUAL_REFRESH,
                StatusRefreshReason.TEST_CONNECTION,
                StatusRefreshReason.AFTER_ACTION -> {
                    statusRequestMutex.lock()
                    true
                }
            }

        if (!lockAcquired) {
            return StatusRefreshOutcome
                .SKIPPED_ALREADY_RUNNING
        }

        try {
            val address = resolveSavedAddress()

            if (address == null) {
                _statusState.update { currentState ->
                    currentState.copy(
                        address = null,
                        connectionState =
                            DeviceConnectionState
                                .NO_SAVED_DEVICE,
                        isRefreshing = false,
                        errorMessage =
                            "No smart outlet address is saved."
                    )
                }

                return StatusRefreshOutcome
                    .NO_SAVED_DEVICE
            }

            val attemptTime = currentTimeMillis()

            _statusState.update { currentState ->
                currentState.copy(
                    address = address,
                    connectionState =
                        if (currentState.status == null) {
                            DeviceConnectionState.CONNECTING
                        } else {
                            currentState.connectionState
                        },
                    isRefreshing = true,
                    lastAttemptEpochMillis = attemptTime,
                    errorMessage =
                        if (
                            reason ==
                            StatusRefreshReason.POLLING
                        ) {
                            currentState.errorMessage
                        } else {
                            null
                        }
                )
            }

            val result = networkFactory
                .createClient(address)
                .getStatus()

            return when (result) {
                is DeviceStatusResult.Success -> {
                    handleSuccessfulStatus(
                        address = address,
                        result = result
                    )

                    StatusRefreshOutcome.SUCCESS
                }

                DeviceStatusResult.Timeout -> {
                    handleFailedStatus(
                        message =
                            "The device status request timed out."
                    )

                    StatusRefreshOutcome.FAILED
                }

                is DeviceStatusResult.InvalidDevice -> {
                    handleFailedStatus(
                        message =
                            "The saved address responded as " +
                                    "${result.receivedDevice}, not " +
                                    "SmartOutlet_ESP8266.",
                        forceOffline = true
                    )

                    StatusRefreshOutcome.FAILED
                }

                is DeviceStatusResult.HttpError -> {
                    handleFailedStatus(
                        message = result.message
                    )

                    StatusRefreshOutcome.FAILED
                }

                is DeviceStatusResult.InvalidResponse -> {
                    handleFailedStatus(
                        message = result.message
                    )

                    StatusRefreshOutcome.FAILED
                }

                is DeviceStatusResult.NetworkError -> {
                    handleFailedStatus(
                        message = result.message
                    )

                    StatusRefreshOutcome.FAILED
                }
            }
        } catch (exception: CancellationException) {
            throw exception
        } finally {
            _statusState.update { currentState ->
                currentState.copy(
                    isRefreshing = false
                )
            }

            statusRequestMutex.unlock()
        }
    }

    private fun observeSavedAddress() {
        scope.launch {
            settingsStore.savedDeviceAddress
                .distinctUntilChanged()
                .collect { address ->
                    val previousAddress =
                        _statusState.value.address

                    if (previousAddress == address) {
                        return@collect
                    }

                    _statusState.update {
                        DeviceStatusRepositoryState(
                            address = address,
                            connectionState =
                                if (address == null) {
                                    DeviceConnectionState
                                        .NO_SAVED_DEVICE
                                } else {
                                    DeviceConnectionState
                                        .CONNECTING
                                }
                        )
                    }
                }
        }
    }

    private suspend fun resolveSavedAddress():
            DeviceAddress? {
        val currentAddress =
            _statusState.value.address

        if (currentAddress != null) {
            return currentAddress
        }

        val storedAddress =
            settingsStore.savedDeviceAddress.first()

        if (storedAddress != null) {
            _statusState.update { currentState ->
                currentState.copy(
                    address = storedAddress,
                    connectionState =
                        DeviceConnectionState.CONNECTING
                )
            }
        }

        return storedAddress
    }

    private fun handleSuccessfulStatus(
        address: DeviceAddress,
        result: DeviceStatusResult.Success
    ) {
        val refreshTime = currentTimeMillis()

        _statusState.update { currentState ->
            currentState.copy(
                address = address,
                status = result.status,
                connectionState =
                    DeviceConnectionState.ONLINE,
                isRefreshing = false,
                consecutiveFailures = 0,
                lastSuccessfulRefreshEpochMillis =
                    refreshTime,
                lastAttemptEpochMillis = refreshTime,
                errorMessage = null
            )
        }
    }

    private fun handleFailedStatus(
        message: String,
        forceOffline: Boolean = false
    ) {
        _statusState.update { currentState ->
            val newFailureCount =
                currentState.consecutiveFailures + 1

            val newConnectionState =
                when {
                    forceOffline -> {
                        DeviceConnectionState.OFFLINE
                    }

                    newFailureCount >=
                            OFFLINE_FAILURE_THRESHOLD -> {
                        DeviceConnectionState.OFFLINE
                    }

                    else -> {
                        DeviceConnectionState.RECONNECTING
                    }
                }

            currentState.copy(
                connectionState = newConnectionState,
                isRefreshing = false,
                consecutiveFailures = newFailureCount,
                errorMessage = message
            )
        }
    }

    private fun pollingDelayMillis(
        failureCount: Int
    ): Long {
        return when (failureCount) {
            0 -> NORMAL_POLL_INTERVAL_MILLISECONDS
            1 -> FIRST_RETRY_MILLISECONDS
            2 -> SECOND_RETRY_MILLISECONDS
            3 -> THIRD_RETRY_MILLISECONDS
            else -> MAX_RETRY_MILLISECONDS
        }
    }

    companion object {
        private const val NORMAL_POLL_INTERVAL_MILLISECONDS =
            2_000L

        private const val FIRST_RETRY_MILLISECONDS =
            2_000L

        private const val SECOND_RETRY_MILLISECONDS =
            5_000L

        private const val THIRD_RETRY_MILLISECONDS =
            10_000L

        private const val MAX_RETRY_MILLISECONDS =
            30_000L

        private const val OFFLINE_FAILURE_THRESHOLD =
            3
    }
}