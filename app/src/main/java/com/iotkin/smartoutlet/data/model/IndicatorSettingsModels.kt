package com.iotkin.smartoutlet.data.model

import kotlinx.serialization.Serializable

@Serializable
data class IndicatorSettingsRequest(
    val enabled: Boolean
)

@Serializable
data class IndicatorSettingsResponse(
    val success: Boolean,
    val indicatorsEnabled: Boolean
)
