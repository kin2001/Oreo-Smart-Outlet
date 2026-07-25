package com.iotkin.smartoutlet.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ApiErrorResponse(
    val success: Boolean,
    val error: String
)