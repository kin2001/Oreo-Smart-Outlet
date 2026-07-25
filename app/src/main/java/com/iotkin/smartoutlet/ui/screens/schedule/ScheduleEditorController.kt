package com.iotkin.smartoutlet.ui.screens.schedule

import com.iotkin.smartoutlet.data.model.RelayNumber
import com.iotkin.smartoutlet.data.model.ScheduleUpdateRequest
import com.iotkin.smartoutlet.data.repository.ScheduleUpdateResult
import com.iotkin.smartoutlet.data.repository.SmartOutletRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ScheduleEditorController(
    private val repository: SmartOutletRepository,
    private val scope: CoroutineScope,
    private val isAnotherWriteRunning: () -> Boolean
) {
    private val _state =
        MutableStateFlow(
            ScheduleEditorUiState()
        )

    val state: StateFlow<ScheduleEditorUiState> =
        _state.asStateFlow()

    fun loadSchedule(
        relay: RelayNumber
    ) {
        val deviceStatus =
            repository.statusState.value.status

        val schedule =
            when (relay) {
                RelayNumber.RELAY_1 ->
                    deviceStatus?.relay1?.schedule

                RelayNumber.RELAY_2 ->
                    deviceStatus?.relay2?.schedule
            }

        _state.value =
            if (schedule == null) {
                ScheduleEditorUiState(
                    relay = relay,
                    error =
                        "Confirmed schedule data is not available yet."
                )
            } else {
                ScheduleEditorUiState.fromSchedule(
                    relay = relay,
                    schedule = schedule
                )
            }
    }

    fun updateEnabled(
        enabled: Boolean
    ) {
        updateEditor {
            copy(
                enabled = enabled
            )
        }
    }

    fun updateOnHour(
        value: String
    ) {
        val sanitized =
            sanitizeNumberInput(
                value = value,
                maximumLength = 2
            )

        updateEditor {
            copy(
                onTime =
                    onTime.copy(
                        hourText = sanitized
                    )
            )
        }
    }

    fun updateOnMinute(
        value: String
    ) {
        val sanitized =
            sanitizeNumberInput(
                value = value,
                maximumLength = 2
            )

        updateEditor {
            copy(
                onTime =
                    onTime.copy(
                        minuteText = sanitized
                    )
            )
        }
    }

    fun updateOnMeridiem(
        meridiem: ScheduleMeridiem
    ) {
        updateEditor {
            copy(
                onTime =
                    onTime.copy(
                        meridiem = meridiem
                    )
            )
        }
    }

    fun updateOffHour(
        value: String
    ) {
        val sanitized =
            sanitizeNumberInput(
                value = value,
                maximumLength = 2
            )

        updateEditor {
            copy(
                offTime =
                    offTime.copy(
                        hourText = sanitized
                    )
            )
        }
    }

    fun updateOffMinute(
        value: String
    ) {
        val sanitized =
            sanitizeNumberInput(
                value = value,
                maximumLength = 2
            )

        updateEditor {
            copy(
                offTime =
                    offTime.copy(
                        minuteText = sanitized
                    )
            )
        }
    }

    fun updateOffMeridiem(
        meridiem: ScheduleMeridiem
    ) {
        updateEditor {
            copy(
                offTime =
                    offTime.copy(
                        meridiem = meridiem
                    )
            )
        }
    }

    fun saveSchedule() {
        val currentState =
            _state.value

        if (
            currentState.isSaving ||
            isAnotherWriteRunning()
        ) {
            _state.update {
                it.copy(
                    error =
                        "Another device action is already running.",
                    message = null
                )
            }

            return
        }

        val relay =
            currentState.relay

        if (relay == null) {
            _state.update {
                it.copy(
                    error =
                        "No outlet schedule is selected.",
                    message = null
                )
            }

            return
        }

        val validationError =
            validateEditor(
                state = currentState
            )

        if (validationError != null) {
            _state.update {
                it.copy(
                    validationError =
                        validationError,
                    message = null,
                    error = null
                )
            }

            return
        }

        val request =
            createRequest(
                state = currentState
            )

        if (request == null) {
            _state.update {
                it.copy(
                    validationError =
                        "Enter valid ON and OFF times.",
                    message = null,
                    error = null
                )
            }

            return
        }

        _state.update {
            it.copy(
                isSaving = true,
                validationError = null,
                message = null,
                error = null
            )
        }

        scope.launch {
            val result =
                repository.updateSchedule(
                    relay = relay,
                    request = request
                )

            _state.value =
                when (result) {
                    is ScheduleUpdateResult.Success -> {
                        ScheduleEditorUiState
                            .fromSchedule(
                                relay = result.relay,
                                schedule =
                                    result.confirmedSchedule,
                                message =
                                    "Outlet ${result.relay.apiValue} schedule saved."
                            )
                    }

                    is ScheduleUpdateResult
                    .ConfirmedButRefreshFailed -> {
                        ScheduleEditorUiState
                            .fromSchedule(
                                relay = result.relay,
                                schedule =
                                    result.confirmedSchedule,
                                message =
                                    "Outlet ${result.relay.apiValue} schedule saved.",
                                error = result.message
                            )
                    }

                    is ScheduleUpdateResult
                    .InvalidRequest -> {
                        currentState.copy(
                            isSaving = false,
                            validationError =
                                result.message,
                            message = null,
                            error = null
                        )
                    }

                    ScheduleUpdateResult
                        .NoSavedDevice -> {
                        currentState.copy(
                            isSaving = false,
                            message = null,
                            error =
                                "No smart outlet address is saved."
                        )
                    }

                    ScheduleUpdateResult
                        .DeviceUnavailable -> {
                        currentState.copy(
                            isSaving = false,
                            message = null,
                            error =
                                "Schedules cannot be saved while the device is offline or showing stale data."
                        )
                    }

                    ScheduleUpdateResult
                        .SkippedAlreadyRunning -> {
                        currentState.copy(
                            isSaving = false,
                            message = null,
                            error =
                                "Another device action is already running."
                        )
                    }

                    is ScheduleUpdateResult.Failed -> {
                        currentState.copy(
                            isSaving = false,
                            message = null,
                            error = result.message
                        )
                    }
                }
        }
    }

    fun clearFeedback() {
        _state.update {
            it.copy(
                validationError = null,
                message = null,
                error = null
            )
        }
    }

    private fun updateEditor(
        transform:
        ScheduleEditorUiState.() ->
        ScheduleEditorUiState
    ) {
        _state.update { currentState ->
            currentState
                .transform()
                .copy(
                    hasUnsavedChanges = true,
                    validationError = null,
                    message = null,
                    error = null
                )
        }
    }

    private fun validateEditor(
        state: ScheduleEditorUiState
    ): String? {
        state.onTime
            .validationError(
                label = "ON time"
            )
            ?.let {
                return it
            }

        state.offTime
            .validationError(
                label = "OFF time"
            )
            ?.let {
                return it
            }

        val onHour =
            state.onTime.to24Hour()

        val onMinute =
            state.onTime.minuteValue()

        val offHour =
            state.offTime.to24Hour()

        val offMinute =
            state.offTime.minuteValue()

        if (
            onHour == offHour &&
            onMinute == offMinute
        ) {
            return "ON and OFF times must be different."
        }

        return null
    }

    private fun createRequest(
        state: ScheduleEditorUiState
    ): ScheduleUpdateRequest? {
        val onHour =
            state.onTime.to24Hour()
                ?: return null

        val onMinute =
            state.onTime.minuteValue()
                ?: return null

        val offHour =
            state.offTime.to24Hour()
                ?: return null

        val offMinute =
            state.offTime.minuteValue()
                ?: return null

        return ScheduleUpdateRequest(
            enabled = state.enabled,
            onHour = onHour,
            onMinute = onMinute,
            offHour = offHour,
            offMinute = offMinute
        )
    }

    private fun sanitizeNumberInput(
        value: String,
        maximumLength: Int
    ): String {
        return value
            .filter {
                    character ->
                character.isDigit()
            }
            .take(maximumLength)
    }
}