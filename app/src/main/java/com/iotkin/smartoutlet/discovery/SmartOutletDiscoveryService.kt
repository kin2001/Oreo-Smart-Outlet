package com.iotkin.smartoutlet.discovery

import kotlinx.coroutines.flow.StateFlow

interface SmartOutletDiscoveryService {

    val state: StateFlow<NsdDiscoveryState>

    fun startDiscovery()

    fun refresh()

    fun stopDiscovery()
}