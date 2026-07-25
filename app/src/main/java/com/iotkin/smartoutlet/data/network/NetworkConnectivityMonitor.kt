package com.iotkin.smartoutlet.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class NetworkConnectivityMonitor(
    context: Context
) {
    private val connectivityManager =
        context.applicationContext
            .getSystemService(
                ConnectivityManager::class.java
            )

    val isWifiAvailable: Flow<Boolean> =
        callbackFlow {
            val activeWifiNetworks =
                mutableSetOf<Network>()

            fun sendCurrentState() {
                val hasWifi =
                    synchronized(
                        activeWifiNetworks
                    ) {
                        activeWifiNetworks
                            .isNotEmpty()
                    }

                trySend(hasWifi)
            }

            val callback =
                object :
                    ConnectivityManager
                    .NetworkCallback() {

                    override fun onAvailable(
                        network: Network
                    ) {
                        synchronized(
                            activeWifiNetworks
                        ) {
                            activeWifiNetworks.add(
                                network
                            )
                        }

                        sendCurrentState()
                    }

                    override fun onLost(
                        network: Network
                    ) {
                        synchronized(
                            activeWifiNetworks
                        ) {
                            activeWifiNetworks.remove(
                                network
                            )
                        }

                        sendCurrentState()
                    }
                }

            val initialWifiNetworks =
                connectivityManager
                    .allNetworks
                    .filter { network ->
                        connectivityManager
                            .getNetworkCapabilities(
                                network
                            )
                            ?.hasTransport(
                                NetworkCapabilities
                                    .TRANSPORT_WIFI
                            ) == true
                    }

            synchronized(activeWifiNetworks) {
                activeWifiNetworks.addAll(
                    initialWifiNetworks
                )
            }

            sendCurrentState()

            val request =
                NetworkRequest.Builder()
                    .addTransportType(
                        NetworkCapabilities
                            .TRANSPORT_WIFI
                    )
                    .build()

            connectivityManager
                .registerNetworkCallback(
                    request,
                    callback
                )

            awaitClose {
                runCatching {
                    connectivityManager
                        .unregisterNetworkCallback(
                            callback
                        )
                }
            }
        }.distinctUntilChanged()
}