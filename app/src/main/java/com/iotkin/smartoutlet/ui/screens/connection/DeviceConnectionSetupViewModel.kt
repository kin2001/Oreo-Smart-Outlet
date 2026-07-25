package com.iotkin.smartoutlet.ui.screens.connection

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.iotkin.smartoutlet.data.model.DeviceAddress
import com.iotkin.smartoutlet.data.model.DeviceAddressValidator
import com.iotkin.smartoutlet.data.network.DeviceStatusResult
import com.iotkin.smartoutlet.data.network.SmartOutletNetworkFactory
import com.iotkin.smartoutlet.data.settings.DeviceSettingsStore
import com.iotkin.smartoutlet.discovery.AndroidNsdDiscoveryService
import com.iotkin.smartoutlet.discovery.DiscoveredSmartOutlet
import com.iotkin.smartoutlet.discovery.SmartOutletDiscoveryCoordinator
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface DeviceConnectionSetupEvent {

    data object DeviceSaved :
        DeviceConnectionSetupEvent

    data object DeviceDisconnected :
        DeviceConnectionSetupEvent
}

class DeviceConnectionSetupViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val networkFactory =
        SmartOutletNetworkFactory()

    private val settingsStore =
        DeviceSettingsStore(application)

    private val discoveryCoordinator =
        SmartOutletDiscoveryCoordinator(
            discoveryService =
                AndroidNsdDiscoveryService(application),
            networkFactory = networkFactory,
            scope = viewModelScope
        )

    private val _uiState = MutableStateFlow(
        DeviceConnectionSetupUiState()
    )

    val uiState: StateFlow<DeviceConnectionSetupUiState> =
        _uiState.asStateFlow()

    private val _events =
        MutableSharedFlow<DeviceConnectionSetupEvent>()

    val events: SharedFlow<DeviceConnectionSetupEvent> =
        _events.asSharedFlow()

    init {
        viewModelScope.launch {
            discoveryCoordinator.state.collect {
                    discoveryState ->

                _uiState.update { currentState ->
                    currentState.copy(
                        discovery = discoveryState
                    )
                }
            }
        }
    }

    fun startDiscovery() {
        discoveryCoordinator.start()
    }

    fun refreshDiscovery() {
        discoveryCoordinator.refresh()
    }

    fun stopDiscovery() {
        discoveryCoordinator.stop()
    }

    fun selectDiscoveredDevice(
        device: DiscoveredSmartOutlet
    ) {
        _uiState.update { currentState ->
            currentState.copy(
                ipAddress = device.address.host,
                port = device.address.port.toString(),
                ipError = null,
                portError = null,
                isDeviceVerified = true,
                verifiedAddress = device.address,
                connectionIssue = null,
                connectionMessage =
                    "${device.serviceName} was found and verified.",
                saveError = null
            )
        }
    }

    fun onIpAddressChange(
        value: String
    ) {
        _uiState.update { currentState ->
            currentState.copy(
                ipAddress = value,
                ipError = null,
                isDeviceVerified = false,
                verifiedAddress = null,
                connectionIssue = null,
                connectionMessage = null,
                saveError = null
            )
        }
    }

    fun onPortChange(
        value: String
    ) {
        _uiState.update { currentState ->
            currentState.copy(
                port = value,
                portError = null,
                isDeviceVerified = false,
                verifiedAddress = null,
                connectionIssue = null,
                connectionMessage = null,
                saveError = null
            )
        }
    }

    fun validateAddress(): DeviceAddress? {
        val currentState = _uiState.value

        val result = DeviceAddressValidator.validate(
            ipInput = currentState.ipAddress,
            portInput = currentState.port
        )

        _uiState.update { state ->
            state.copy(
                ipError = result.ipError,
                portError = result.portError,
                isDeviceVerified = false,
                verifiedAddress = null,
                connectionIssue = null,
                connectionMessage = null,
                saveError = null
            )
        }

        return result.address
    }

    fun testConnection() {
        if (
            _uiState.value.isTestingConnection ||
            _uiState.value.isSavingDevice
        ) {
            return
        }

        val address = validateAddress()
            ?: return

        _uiState.update { currentState ->
            currentState.copy(
                isTestingConnection = true,
                isDeviceVerified = false,
                verifiedAddress = null,
                connectionIssue = null,
                connectionMessage = null,
                saveError = null
            )
        }

        viewModelScope.launch {
            val result = networkFactory
                .createClient(address)
                .getStatus()

            _uiState.update { currentState ->
                when (result) {
                    is DeviceStatusResult.Success -> {
                        currentState.copy(
                            isTestingConnection = false,
                            isDeviceVerified = true,
                            verifiedAddress = address,
                            connectionIssue = null,
                            connectionMessage =
                                "${result.status.device} responded successfully."
                        )
                    }

                    DeviceStatusResult.Timeout -> {
                        currentState.copy(
                            isTestingConnection = false,
                            isDeviceVerified = false,
                            verifiedAddress = null,
                            connectionIssue =
                                ConnectionIssueType.TIMEOUT,
                            connectionMessage =
                                "The smart outlet did not respond before the request timed out."
                        )
                    }

                    is DeviceStatusResult.InvalidDevice -> {
                        currentState.copy(
                            isTestingConnection = false,
                            isDeviceVerified = false,
                            verifiedAddress = null,
                            connectionIssue =
                                ConnectionIssueType.INVALID_DEVICE,
                            connectionMessage =
                                "The address responded as ${result.receivedDevice}, not SmartOutlet_ESP8266."
                        )
                    }

                    is DeviceStatusResult.HttpError -> {
                        currentState.connectionFailure(
                            message = result.message
                        )
                    }

                    is DeviceStatusResult.InvalidResponse -> {
                        currentState.connectionFailure(
                            message = result.message
                        )
                    }

                    is DeviceStatusResult.NetworkError -> {
                        currentState.connectionFailure(
                            message = result.message
                        )
                    }
                }
            }
        }
    }


    fun saveDevice() {
        val currentState = _uiState.value
        val address = currentState.verifiedAddress

        if (
            address == null ||
            !currentState.isDeviceVerified ||
            currentState.isTestingConnection ||
            currentState.isSavingDevice
        ) {
            return
        }

        _uiState.update { state ->
            state.copy(
                isSavingDevice = true,
                saveError = null
            )
        }

        viewModelScope.launch {
            try {
                settingsStore.saveDeviceAddress(
                    address
                )

                _uiState.update { state ->
                    state.copy(
                        isSavingDevice = false,
                        saveError = null
                    )
                }

                discoveryCoordinator.stop()

                _events.emit(
                    DeviceConnectionSetupEvent.DeviceSaved
                )
            } catch (exception: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isSavingDevice = false,
                        saveError =
                            "The device address could not be saved."
                    )
                }
            }
        }
    }
    fun disconnectDevice() {
        viewModelScope.launch {
            settingsStore.clearDeviceAddress()

            _uiState.value =
                DeviceConnectionSetupUiState()

            discoveryCoordinator.refresh()

            _events.emit(
                DeviceConnectionSetupEvent.DeviceDisconnected
            )
        }
    }

    override fun onCleared() {
        discoveryCoordinator.stop()
        super.onCleared()
    }

    private fun DeviceConnectionSetupUiState
            .connectionFailure(
        message: String
    ): DeviceConnectionSetupUiState {

        return copy(
            isTestingConnection = false,
            isDeviceVerified = false,
            verifiedAddress = null,
            connectionIssue =
                ConnectionIssueType.CONNECTION_FAILED,
            connectionMessage = message
        )
    }
}