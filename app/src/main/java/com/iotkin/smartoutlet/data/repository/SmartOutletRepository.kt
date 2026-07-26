package com.iotkin.smartoutlet.data.repository

import com.iotkin.smartoutlet.data.model.DeviceAddress
import com.iotkin.smartoutlet.data.network.DeviceStatusResult
import com.iotkin.smartoutlet.data.network.SmartOutletNetworkFactory
import com.iotkin.smartoutlet.data.settings.AppSettings
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
import com.iotkin.smartoutlet.data.model.RelayNumber
import com.iotkin.smartoutlet.data.network.RelayApiResult
import com.iotkin.smartoutlet.data.model.ScheduleUpdateRequest
import com.iotkin.smartoutlet.data.network.ScheduleApiResult
import kotlinx.coroutines.cancelAndJoin

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
    private var pollingRestartJob: Job? = null

    private var foregroundPollingRequested =
        false

    private var normalPollingIntervalMilliseconds =
        AppSettings
            .DEFAULT_POLLING_INTERVAL_SECONDS *
                1_000L

    init {
        observeSavedAddress()
        observePollingInterval()
    }

    fun startForegroundPolling() {
        foregroundPollingRequested = true

        if (pollingJob?.isActive == true) {
            return
        }

        pollingJob = scope.launch {
            refreshStatus(
                reason =
                    StatusRefreshReason
                        .FOREGROUND_START
            )

            while (isActive) {
                delay(
                    pollingDelayMillis(
                        failureCount =
                            _statusState.value
                                .consecutiveFailures
                    )
                )

                refreshStatus(
                    reason =
                        StatusRefreshReason.POLLING
                )
            }
        }
    }

    fun stopForegroundPolling() {
        foregroundPollingRequested = false

        pollingRestartJob?.cancel()
        pollingRestartJob = null

        pollingJob?.cancel()
        pollingJob = null

        _statusState.update { currentState ->
            currentState.copy(
                isRefreshing = false
            )
        }
    }

    fun onWifiAvailabilityChanged(
        isAvailable: Boolean
    ) {
        if (isAvailable) {
            restartForegroundPollingImmediately()
            return
        }

        val currentState =
            _statusState.value

        if (
            currentState.address == null ||
            currentState.connectionState ==
            DeviceConnectionState
                .NO_SAVED_DEVICE
        ) {
            return
        }

        when (currentState.connectionState) {
            DeviceConnectionState.ONLINE,
            DeviceConnectionState.CONNECTING -> {
                handleFailedStatus(
                    message =
                        "The phone is not connected to Wi-Fi."
                )
            }

            DeviceConnectionState.RECONNECTING,
            DeviceConnectionState.OFFLINE -> {
                _statusState.update {
                    it.copy(
                        errorMessage =
                            "The phone is not connected to Wi-Fi."
                    )
                }
            }

            DeviceConnectionState
                .NO_SAVED_DEVICE -> Unit
        }
    }

    suspend fun requestTimeSync():
            DeviceActionResult {

        if (!writeRequestMutex.tryLock()) {
            return DeviceActionResult
                .SkippedAlreadyRunning
        }

        try {
            val address =
                resolveSavedAddress()
                    ?: return DeviceActionResult
                        .NoSavedDevice

            val currentState =
                _statusState.value

            if (
                currentState.connectionState !=
                DeviceConnectionState.ONLINE ||
                currentState.isStale
            ) {
                return DeviceActionResult.NetworkError(
                    message =
                        "Time synchronization is unavailable while the device is offline, reconnecting, or showing stale data."
                )
            }

            val result =
                networkFactory
                    .createClient(address)
                    .requestTimeSync()

            if (
                result is DeviceActionResult.Success
            ) {
                refreshStatus(
                    reason =
                        StatusRefreshReason
                            .AFTER_ACTION
                )
            }

            return result
        } finally {
            writeRequestMutex.unlock()
        }
    }

    suspend fun setRelayState(
        relay: RelayNumber,
        desiredState: Boolean
    ): RelayCommandResult {
        if (!writeRequestMutex.tryLock()) {
            return RelayCommandResult
                .SkippedAlreadyRunning
        }

        try {
            val currentState =
                _statusState.value

            if (
                currentState.connectionState !=
                DeviceConnectionState.ONLINE ||
                currentState.isStale
            ) {
                return RelayCommandResult
                    .DeviceUnavailable
            }

            val address =
                resolveSavedAddress()
                    ?: return RelayCommandResult
                        .NoSavedDevice

            val result =
                networkFactory
                    .createClient(address)
                    .setRelayState(
                        relay = relay,
                        desiredState =
                            desiredState
                    )

            return when (result) {
                is RelayApiResult.Success -> {
                    val refreshOutcome =
                        refreshStatus(
                            reason =
                                StatusRefreshReason
                                    .AFTER_ACTION
                        )

                    when (refreshOutcome) {
                        StatusRefreshOutcome.SUCCESS -> {
                            RelayCommandResult.Success(
                                relay = relay,
                                confirmedState =
                                    result
                                        .confirmedState
                            )
                        }

                        else -> {
                            RelayCommandResult
                                .ConfirmedButRefreshFailed(
                                    relay = relay,
                                    confirmedState =
                                        result
                                            .confirmedState,
                                    message =
                                        _statusState.value
                                            .errorMessage
                                            ?: "The relay command was confirmed, but the latest status could not be loaded."
                                )
                        }
                    }
                }

                RelayApiResult.Timeout -> {
                    RelayCommandResult.Failed(
                        message =
                            "The relay command timed out. Its final state is unknown until status polling reconnects. The command will not be sent again automatically."
                    )
                }

                is RelayApiResult.HttpError -> {
                    RelayCommandResult.Failed(
                        message = result.message
                    )
                }

                is RelayApiResult.InvalidResponse -> {
                    RelayCommandResult.Failed(
                        message = result.message
                    )
                }

                is RelayApiResult.NetworkError -> {
                    RelayCommandResult.Failed(
                        message = result.message
                    )
                }
            }
        } finally {
            writeRequestMutex.unlock()
        }
    }

    suspend fun updateSchedule(
        relay: RelayNumber,
        request: ScheduleUpdateRequest
    ): ScheduleUpdateResult {
        val validationError =
            request.validationError()

        if (validationError != null) {
            return ScheduleUpdateResult.InvalidRequest(
                message = validationError
            )
        }

        if (!writeRequestMutex.tryLock()) {
            return ScheduleUpdateResult
                .SkippedAlreadyRunning
        }

        try {
            val currentState =
                _statusState.value

            if (
                currentState.connectionState !=
                DeviceConnectionState.ONLINE ||
                currentState.isStale
            ) {
                return ScheduleUpdateResult
                    .DeviceUnavailable
            }

            val address =
                resolveSavedAddress()
                    ?: return ScheduleUpdateResult
                        .NoSavedDevice

            val result =
                networkFactory
                    .createClient(address)
                    .updateSchedule(
                        relay = relay,
                        request = request
                    )

            return when (result) {
                is ScheduleApiResult.Success -> {
                    val refreshOutcome =
                        refreshStatus(
                            reason =
                                StatusRefreshReason
                                    .AFTER_ACTION
                        )

                    when (refreshOutcome) {
                        StatusRefreshOutcome.SUCCESS -> {
                            ScheduleUpdateResult.Success(
                                relay = result.relay,
                                confirmedSchedule =
                                    result.confirmedSchedule
                            )
                        }

                        else -> {
                            ScheduleUpdateResult
                                .ConfirmedButRefreshFailed(
                                    relay = result.relay,
                                    confirmedSchedule =
                                        result.confirmedSchedule,
                                    message =
                                        _statusState.value
                                            .errorMessage
                                            ?: "The schedule was saved, but the latest device status could not be loaded."
                                )
                        }
                    }
                }

                is ScheduleApiResult.InvalidRequest -> {
                    ScheduleUpdateResult.InvalidRequest(
                        message = result.message
                    )
                }

                ScheduleApiResult.Timeout -> {
                    ScheduleUpdateResult.Failed(
                        message =
                            "The schedule request timed out. Check the device status before trying again."
                    )
                }

                is ScheduleApiResult.HttpError -> {
                    ScheduleUpdateResult.Failed(
                        message = result.message
                    )
                }

                is ScheduleApiResult.InvalidResponse -> {
                    ScheduleUpdateResult.Failed(
                        message = result.message
                    )
                }

                is ScheduleApiResult.NetworkError -> {
                    ScheduleUpdateResult.Failed(
                        message = result.message
                    )
                }
            }
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

    private fun restartForegroundPollingImmediately() {
        if (!foregroundPollingRequested) {
            return
        }

        pollingRestartJob?.cancel()

        pollingRestartJob = scope.launch {
            pollingJob?.cancelAndJoin()
            pollingJob = null

            if (foregroundPollingRequested) {
                startForegroundPolling()
            }
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

                    val shouldRestartPolling =
                        foregroundPollingRequested

                    if (shouldRestartPolling) {
                        pollingJob?.cancelAndJoin()
                        pollingJob = null
                    }

                    _statusState.value =
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

                    if (shouldRestartPolling) {
                        startForegroundPolling()
                    }
                }
        }
    }

    private fun observePollingInterval() {
        scope.launch {
            settingsStore.appSettings
                .collect { settings ->
                    val newInterval =
                        settings
                            .pollingIntervalSeconds *
                                1_000L

                    if (
                        normalPollingIntervalMilliseconds ==
                        newInterval
                    ) {
                        return@collect
                    }

                    normalPollingIntervalMilliseconds =
                        newInterval

                    restartForegroundPollingImmediately()
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
            0 ->
                normalPollingIntervalMilliseconds

            1 -> FIRST_RETRY_MILLISECONDS
            2 -> SECOND_RETRY_MILLISECONDS
            3 -> THIRD_RETRY_MILLISECONDS
            else -> MAX_RETRY_MILLISECONDS
        }
    }

    companion object {
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
