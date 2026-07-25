package com.iotkin.smartoutlet.data.model

import kotlinx.serialization.Serializable

@Serializable
data class TimeSyncResponse(
    val success: Boolean,
    val requested: Boolean
)