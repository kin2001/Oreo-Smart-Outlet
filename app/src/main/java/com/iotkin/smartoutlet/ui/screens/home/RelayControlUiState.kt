package com.iotkin.smartoutlet.ui.screens.home

import com.iotkin.smartoutlet.data.model.RelayNumber

data class RelayControlUiState(
    val updatingRelay: RelayNumber? = null,
    val message: String? = null,
    val error: String? = null
) {
    val isAnyRelayUpdating: Boolean
        get() = updatingRelay != null

    fun isUpdating(
        relay: RelayNumber
    ): Boolean {
        return updatingRelay == relay
    }
}