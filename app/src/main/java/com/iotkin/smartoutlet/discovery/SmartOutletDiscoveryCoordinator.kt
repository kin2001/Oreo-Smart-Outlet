package com.iotkin.smartoutlet.discovery

import com.iotkin.smartoutlet.data.network.DeviceStatusResult
import com.iotkin.smartoutlet.data.network.SmartOutletNetworkFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SmartOutletDiscoveryCoordinator(
    private val discoveryService: SmartOutletDiscoveryService,
    private val networkFactory: SmartOutletNetworkFactory,
    private val scope: CoroutineScope
) {

    private val _state =
        MutableStateFlow(DeviceDiscoveryUiState())

    val state: StateFlow<DeviceDiscoveryUiState> =
        _state.asStateFlow()

    private val processedCandidateKeys =
        mutableSetOf<String>()

    private val validationJobs =
        mutableMapOf<String, Job>()

    private var searchWindowJob: Job? = null

    init {
        scope.launch {
            discoveryService.state.collect { nsdState ->
                processNsdState(nsdState)
            }
        }
    }

    fun start() {
        resetSearchState()
        discoveryService.startDiscovery()
        startSearchWindowTimer()
    }

    fun refresh() {
        resetSearchState()
        discoveryService.refresh()
        startSearchWindowTimer()
    }

    fun stop() {
        searchWindowJob?.cancel()
        searchWindowJob = null

        validationJobs.values.forEach { job ->
            job.cancel()
        }

        validationJobs.clear()
        discoveryService.stopDiscovery()

        _state.update { currentState ->
            currentState.copy(
                isDiscovering = false,
                isValidating = false
            )
        }
    }

    private fun resetSearchState() {
        searchWindowJob?.cancel()

        validationJobs.values.forEach { job ->
            job.cancel()
        }

        validationJobs.clear()
        processedCandidateKeys.clear()

        _state.value = DeviceDiscoveryUiState(
            isDiscovering = true
        )
    }

    private fun startSearchWindowTimer() {
        searchWindowJob = scope.launch {
            delay(DISCOVERY_WINDOW_MILLISECONDS)

            _state.update { currentState ->
                currentState.copy(
                    hasCompletedSearchWindow = true
                )
            }
        }
    }

    private fun processNsdState(
        nsdState: NsdDiscoveryState
    ) {
        val activeCandidateKeys =
            nsdState.candidates
                .map { candidate ->
                    candidate.stableKey
                }
                .toSet()

        processedCandidateKeys.retainAll(
            activeCandidateKeys
        )

        val lostValidationKeys =
            validationJobs.keys.filterNot { key ->
                key in activeCandidateKeys
            }

        lostValidationKeys.forEach { key ->
            validationJobs.remove(key)?.cancel()
        }

        _state.update { currentState ->
            currentState.copy(
                isDiscovering = nsdState.isDiscovering,
                isValidating = validationJobs.isNotEmpty(),
                devices = currentState.devices.filter { device ->
                    device.stableKey in activeCandidateKeys
                },
                errorMessage = nsdState.errorMessage
            )
        }

        nsdState.candidates.forEach { candidate ->
            validateCandidateIfNeeded(candidate)
        }
    }

    private fun validateCandidateIfNeeded(
        candidate: DiscoveredServiceCandidate
    ) {
        val key = candidate.stableKey

        if (
            key in processedCandidateKeys ||
            key in validationJobs
        ) {
            return
        }

        val validationJob = scope.launch(
            start = CoroutineStart.LAZY
        ) {
            try {
                val result = networkFactory
                    .createClient(candidate.address)
                    .getStatus()

                processedCandidateKeys.add(key)

                if (result is DeviceStatusResult.Success) {
                    val validatedDevice =
                        DiscoveredSmartOutlet(
                            serviceName =
                                candidate.serviceName,
                            address =
                                candidate.address,
                            deviceId =
                                result.status.deviceId,
                            firmwareVersion =
                                result.status.firmwareVersion,
                            rssi =
                                result.status.rssi
                        )

                    addValidatedDevice(
                        validatedDevice
                    )
                }
            } finally {
                validationJobs.remove(key)

                _state.update { currentState ->
                    currentState.copy(
                        isValidating =
                            validationJobs.isNotEmpty()
                    )
                }
            }
        }

        validationJobs[key] = validationJob

        _state.update { currentState ->
            currentState.copy(
                isValidating = true
            )
        }

        validationJob.start()
    }

    private fun addValidatedDevice(
        device: DiscoveredSmartOutlet
    ) {
        _state.update { currentState ->
            val devicesWithoutDuplicate =
                currentState.devices.filterNot { existing ->
                    existing.stableKey ==
                            device.stableKey ||
                            (
                                    device.deviceId.isNotBlank() &&
                                            existing.deviceId ==
                                            device.deviceId
                                    )
                }

            currentState.copy(
                devices =
                    (devicesWithoutDuplicate + device)
                        .sortedBy { discoveredDevice ->
                            discoveredDevice
                                .serviceName
                                .lowercase()
                        }
            )
        }
    }

    companion object {
        private const val DISCOVERY_WINDOW_MILLISECONDS =
            8_000L
    }
}