package com.iotkin.smartoutlet.data.model

import kotlinx.serialization.Serializable

@Serializable
data class DeviceStatusResponse(
    val device: String,
    val success: Boolean,
    val apiVersion: Int,
    val firmwareVersion: String,
    val deviceId: String,
    val uptimeSeconds: Long,
    val freeHeapBytes: Long,
    val freeSketchSpaceBytes: Long,
    val resetReason: String,
    val otaReady: Boolean,
    val apiActive: Boolean,
    val online: Boolean,
    val apiPort: Int,
    val ip: String,
    val ssid: String,
    val rssi: Int,
    val timeValid: Boolean,
    val philippineTime: String,
    val relay1: RelayStatusResponse,
    val relay2: RelayStatusResponse
)

@Serializable
data class RelayStatusResponse(
    val state: Boolean,
    val schedule: RelayScheduleResponse
)

@Serializable
data class RelayScheduleResponse(
    val enabled: Boolean,
    val onHour: Int,
    val onMinute: Int,
    val offHour: Int,
    val offMinute: Int
)