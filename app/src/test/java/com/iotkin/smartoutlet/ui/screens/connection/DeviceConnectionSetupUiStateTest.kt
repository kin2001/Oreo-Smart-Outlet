package com.iotkin.smartoutlet.ui.screens.connection

import com.iotkin.smartoutlet.data.model.DeviceAddress
import org.junit.Assert.assertEquals
import org.junit.Test

class DeviceConnectionSetupUiStateTest {

    @Test
    fun savedDeviceStatusRequiresLiveVerification() {
        val savedState =
            DeviceConnectionSetupUiState(
                savedAddress =
                    DeviceAddress(
                        host = "192.168.8.113",
                        port = 8080
                    )
            )

        assertEquals(
            SavedDeviceConnectionStatus.SAVED,
            savedState.savedDeviceConnectionStatus
        )
        assertEquals(
            SavedDeviceConnectionStatus.RECONNECTING,
            savedState.copy(
                isTestingConnection = true
            ).savedDeviceConnectionStatus
        )
        assertEquals(
            SavedDeviceConnectionStatus.ONLINE,
            savedState.copy(
                isDeviceVerified = true,
                hasVerifiedLiveResponse = true
            ).savedDeviceConnectionStatus
        )
        assertEquals(
            SavedDeviceConnectionStatus.OFFLINE,
            savedState.copy(
                connectionIssue =
                    ConnectionIssueType.TIMEOUT
            ).savedDeviceConnectionStatus
        )
        assertEquals(
            SavedDeviceConnectionStatus.STALE,
            savedState.copy(
                hasVerifiedLiveResponse = true,
                connectionIssue =
                    ConnectionIssueType.TIMEOUT
            ).savedDeviceConnectionStatus
        )
    }
}
