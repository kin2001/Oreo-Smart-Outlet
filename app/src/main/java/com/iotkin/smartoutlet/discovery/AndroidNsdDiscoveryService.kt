package com.iotkin.smartoutlet.discovery

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.Looper
import java.util.ArrayDeque
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AndroidNsdDiscoveryService(
    context: Context
) : SmartOutletDiscoveryService {

    private val applicationContext =
        context.applicationContext

    private val nsdManager =
        applicationContext.getSystemService(
            Context.NSD_SERVICE
        ) as NsdManager

    private val wifiManager =
        applicationContext.getSystemService(
            Context.WIFI_SERVICE
        ) as WifiManager

    private val mainHandler =
        Handler(Looper.getMainLooper())

    private val _state =
        MutableStateFlow(NsdDiscoveryState())

    override val state: StateFlow<NsdDiscoveryState> =
        _state.asStateFlow()

    private val pendingServices =
        ArrayDeque<NsdServiceInfo>()

    private val knownServiceNames =
        mutableSetOf<String>()

    private var discoveryListener:
            NsdManager.DiscoveryListener? = null

    private var multicastLock:
            WifiManager.MulticastLock? = null

    private var resolvingServiceName: String? = null

    private var restartAfterStop = false

    override fun startDiscovery() {
        runOnMain {
            startDiscoveryOnMain()
        }
    }

    override fun refresh() {
        runOnMain {
            _state.update { currentState ->
                currentState.copy(
                    candidates = emptyList(),
                    errorMessage = null
                )
            }

            if (discoveryListener == null) {
                startDiscoveryOnMain()
            } else {
                stopDiscoveryOnMain(
                    restartWhenStopped = true
                )
            }
        }
    }

    override fun stopDiscovery() {
        runOnMain {
            stopDiscoveryOnMain(
                restartWhenStopped = false
            )
        }
    }

    private fun startDiscoveryOnMain() {
        if (discoveryListener != null) {
            return
        }

        restartAfterStop = false
        pendingServices.clear()
        knownServiceNames.clear()
        resolvingServiceName = null

        _state.update { currentState ->
            currentState.copy(
                isDiscovering = true,
                candidates = emptyList(),
                errorMessage = null
            )
        }

        try {
            acquireMulticastLock()

            val listener = createDiscoveryListener()
            discoveryListener = listener

            nsdManager.discoverServices(
                SERVICE_TYPE,
                NsdManager.PROTOCOL_DNS_SD,
                listener
            )
        } catch (exception: SecurityException) {
            failDiscovery(
                message = "Local network discovery permission was denied."
            )
        } catch (exception: IllegalArgumentException) {
            failDiscovery(
                message = "Device discovery could not be started."
            )
        }
    }

    private fun stopDiscoveryOnMain(
        restartWhenStopped: Boolean
    ) {
        restartAfterStop = restartWhenStopped

        val listener = discoveryListener

        if (listener == null) {
            cleanUpDiscovery()

            if (restartWhenStopped) {
                startDiscoveryOnMain()
            }

            return
        }

        try {
            nsdManager.stopServiceDiscovery(listener)
        } catch (exception: IllegalArgumentException) {
            finishStoppingDiscovery()
        } catch (exception: SecurityException) {
            finishStoppingDiscovery()
        }
    }

    private fun createDiscoveryListener():
            NsdManager.DiscoveryListener {

        return object : NsdManager.DiscoveryListener {

            override fun onDiscoveryStarted(
                serviceType: String
            ) {
                if (discoveryListener !== this) {
                    return
                }

                _state.update { currentState ->
                    currentState.copy(
                        isDiscovering = true,
                        errorMessage = null
                    )
                }
            }

            override fun onServiceFound(
                serviceInfo: NsdServiceInfo
            ) {
                if (discoveryListener !== this) {
                    return
                }

                val discoveredType =
                    normalizeServiceType(
                        serviceInfo.serviceType
                    )

                if (
                    discoveredType !=
                    normalizeServiceType(SERVICE_TYPE)
                ) {
                    return
                }

                enqueueService(serviceInfo)
            }

            override fun onServiceLost(
                serviceInfo: NsdServiceInfo
            ) {
                if (discoveryListener !== this) {
                    return
                }

                removeService(
                    serviceName = serviceInfo.serviceName
                )
            }

            override fun onStartDiscoveryFailed(
                serviceType: String,
                errorCode: Int
            ) {
                if (discoveryListener !== this) {
                    return
                }

                failDiscovery(
                    message =
                        "Device discovery failed to start. Error $errorCode."
                )
            }

            override fun onStopDiscoveryFailed(
                serviceType: String,
                errorCode: Int
            ) {
                if (discoveryListener !== this) {
                    return
                }

                finishStoppingDiscovery()
            }

            override fun onDiscoveryStopped(
                serviceType: String
            ) {
                if (discoveryListener !== this) {
                    return
                }

                finishStoppingDiscovery()
            }
        }
    }

    private fun enqueueService(
        serviceInfo: NsdServiceInfo
    ) {
        val serviceName =
            serviceInfo.serviceName.trim()

        if (serviceName.isBlank()) {
            return
        }

        if (!knownServiceNames.add(serviceName)) {
            return
        }

        pendingServices.addLast(serviceInfo)
        resolveNextService()
    }

    @Suppress("DEPRECATION")
    private fun resolveNextService() {
        if (resolvingServiceName != null) {
            return
        }

        val serviceInfo =
            pendingServices.pollFirst()
                ?: return

        val serviceName =
            serviceInfo.serviceName.trim()

        if (!knownServiceNames.contains(serviceName)) {
            resolveNextService()
            return
        }

        resolvingServiceName = serviceName

        try {
            nsdManager.resolveService(
                serviceInfo,
                object : NsdManager.ResolveListener {

                    override fun onResolveFailed(
                        failedService: NsdServiceInfo,
                        errorCode: Int
                    ) {
                        runOnMain {
                            knownServiceNames.remove(
                                serviceName
                            )

                            resolvingServiceName = null
                            resolveNextService()
                        }
                    }

                    override fun onServiceResolved(
                        resolvedService: NsdServiceInfo
                    ) {
                        runOnMain {
                            handleResolvedService(
                                resolvedService
                            )

                            resolvingServiceName = null
                            resolveNextService()
                        }
                    }
                }
            )
        } catch (exception: IllegalArgumentException) {
            knownServiceNames.remove(serviceName)
            resolvingServiceName = null
            resolveNextService()
        }
    }

    @Suppress("DEPRECATION")
    private fun handleResolvedService(
        serviceInfo: NsdServiceInfo
    ) {
        val serviceName =
            serviceInfo.serviceName.trim()

        if (!knownServiceNames.contains(serviceName)) {
            return
        }

        val host = serviceInfo.host
            ?.hostAddress
            ?.substringBefore("%")
            ?.trim()
            .orEmpty()

        val port = serviceInfo.port

        if (
            host.isBlank() ||
            port !in 1..65535
        ) {
            return
        }

        val candidate = DiscoveredServiceCandidate(
            serviceName = serviceName,
            host = host,
            port = port
        )

        _state.update { currentState ->
            val withoutDuplicates =
                currentState.candidates.filterNot {
                        existing ->
                    existing.stableKey ==
                            candidate.stableKey ||
                            existing.serviceName ==
                            candidate.serviceName
                }

            currentState.copy(
                candidates =
                    (withoutDuplicates + candidate)
                        .sortedBy {
                            it.serviceName.lowercase()
                        }
            )
        }
    }

    private fun removeService(
        serviceName: String
    ) {
        knownServiceNames.remove(serviceName)

        pendingServices.removeAll {
            it.serviceName == serviceName
        }

        _state.update { currentState ->
            currentState.copy(
                candidates =
                    currentState.candidates.filterNot {
                        it.serviceName == serviceName
                    }
            )
        }
    }

    private fun failDiscovery(
        message: String
    ) {
        restartAfterStop = false
        cleanUpDiscovery()

        _state.update { currentState ->
            currentState.copy(
                isDiscovering = false,
                errorMessage = message
            )
        }
    }

    private fun finishStoppingDiscovery() {
        val shouldRestart = restartAfterStop
        restartAfterStop = false

        cleanUpDiscovery()

        if (shouldRestart) {
            startDiscoveryOnMain()
        }
    }

    private fun cleanUpDiscovery() {
        discoveryListener = null
        pendingServices.clear()
        knownServiceNames.clear()
        resolvingServiceName = null

        releaseMulticastLock()

        _state.update { currentState ->
            currentState.copy(
                isDiscovering = false
            )
        }
    }

    private fun acquireMulticastLock() {
        if (multicastLock?.isHeld == true) {
            return
        }

        multicastLock =
            wifiManager.createMulticastLock(
                MULTICAST_LOCK_TAG
            ).apply {
                setReferenceCounted(false)
                acquire()
            }
    }

    private fun releaseMulticastLock() {
        multicastLock?.let { lock ->
            if (lock.isHeld) {
                lock.release()
            }
        }

        multicastLock = null
    }

    private fun normalizeServiceType(
        value: String
    ): String {
        return value
            .trim()
            .removeSuffix(".")
            .lowercase()
    }

    private fun runOnMain(
        action: () -> Unit
    ) {
        if (
            Looper.myLooper() ==
            Looper.getMainLooper()
        ) {
            action()
        } else {
            mainHandler.post(action)
        }
    }

    companion object {
        const val SERVICE_TYPE =
            "_smartoutlet._tcp"

        private const val MULTICAST_LOCK_TAG =
            "OreoSmartOutletDiscovery"
    }
}