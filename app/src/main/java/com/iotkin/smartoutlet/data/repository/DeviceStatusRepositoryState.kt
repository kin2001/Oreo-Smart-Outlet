package com.iotkin.smartoutlet.data.repository

import com.iotkin.smartoutlet.data.model.DeviceAddress
import com.iotkin.smartoutlet.data.model.DeviceStatusResponse

enum class DeviceConnectionState {
    NO_SAVED_DEVICE,
    CONNECTING,
    ONLINE,
    RECONNECTING,
    OFFLINE
}

enum class StatusRefreshReason {
    FOREGROUND_START,
    POLLING,
    MANUAL_REFRESH,
    TEST_CONNECTION,
    AFTER_ACTION
}

enum class StatusRefreshOutcome {
    SUCCESS,
    FAILED,
    NO_SAVED_DEVICE,
    SKIPPED_ALREADY_RUNNING
}

data class DeviceStatusRepositoryState(
    val address: DeviceAddress? = null,
    val status: DeviceStatusResponse? = null,
    val connectionState: DeviceConnectionState =
        DeviceConnectionState.NO_SAVED_DEVICE,
    val isRefreshing: Boolean = false,
    val consecutiveFailures: Int = 0,
    val lastSuccessfulRefreshEpochMillis: Long? = null,
    val lastAttemptEpochMillis: Long? = null,
    val errorMessage: String? = null
) {
    val hasConfirmedStatus: Boolean
        get() = status != null

    val isInitialLoading: Boolean
        get() = isRefreshing && status == null

    val isStale: Boolean
        get() = status != null &&
                connectionState != DeviceConnectionState.ONLINE
}