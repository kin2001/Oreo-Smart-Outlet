package com.iotkin.smartoutlet.data.repository

import com.iotkin.smartoutlet.data.model.RelayNumber

sealed interface RelayCommandResult {

    data class Success(
        val relay: RelayNumber,
        val confirmedState: Boolean
    ) : RelayCommandResult

    data class ConfirmedButRefreshFailed(
        val relay: RelayNumber,
        val confirmedState: Boolean,
        val message: String
    ) : RelayCommandResult

    data object NoSavedDevice :
        RelayCommandResult

    data object DeviceUnavailable :
        RelayCommandResult

    data object SkippedAlreadyRunning :
        RelayCommandResult

    data class Failed(
        val message: String
    ) : RelayCommandResult
}