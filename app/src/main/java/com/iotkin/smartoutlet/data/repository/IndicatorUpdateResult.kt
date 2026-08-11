package com.iotkin.smartoutlet.data.repository

sealed interface IndicatorUpdateResult {

    data class Success(
        val confirmedEnabled: Boolean
    ) : IndicatorUpdateResult

    data class ConfirmedButRefreshFailed(
        val confirmedEnabled: Boolean,
        val message: String
    ) : IndicatorUpdateResult

    data object NoSavedDevice : IndicatorUpdateResult

    data object DeviceUnavailable : IndicatorUpdateResult

    data object UnsupportedFirmware : IndicatorUpdateResult

    data object SkippedAlreadyRunning : IndicatorUpdateResult

    data class Failed(
        val message: String
    ) : IndicatorUpdateResult
}
