package com.iotkin.smartoutlet.ui.screens.diagnostics

import org.junit.Assert.assertEquals
import org.junit.Test

class DiagnosticsFormattingTest {
    @Test
    fun wifiQualityUsesConsumerFriendlyRanges() {
        assertEquals(
            "Strong",
            wifiQualityLabel(-50)
        )
        assertEquals(
            "Good",
            wifiQualityLabel(-51)
        )
        assertEquals(
            "Good",
            wifiQualityLabel(-60)
        )
        assertEquals(
            "Fair",
            wifiQualityLabel(-61)
        )
        assertEquals(
            "Fair",
            wifiQualityLabel(-70)
        )
        assertEquals(
            "Weak",
            wifiQualityLabel(-71)
        )
    }
}
