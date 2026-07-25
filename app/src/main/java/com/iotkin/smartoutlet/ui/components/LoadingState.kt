package com.iotkin.smartoutlet.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iotkin.smartoutlet.ui.theme.OreoShapeTokens
import com.iotkin.smartoutlet.ui.theme.OreoSmartOutletTheme
import com.iotkin.smartoutlet.ui.theme.OreoSpacing

@Composable
fun OreoLoadingState(
    message: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = OreoShapeTokens.ExtraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(
                alpha = 0.3f
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(
                OreoSpacing.StackLarge
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                OreoSpacing.StackMedium
            )
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(
    name = "Loading State Light",
    showBackground = true
)
@Composable
private fun LoadingStateLightPreview() {
    OreoSmartOutletTheme(
        darkTheme = false
    ) {
        OreoLoadingState(
            message = "Connecting to Smart Outlet",
            modifier = Modifier.padding(
                OreoSpacing.ScreenMargin
            )
        )
    }
}

@Preview(
    name = "Loading State Dark",
    showBackground = true,
    backgroundColor = 0xFF0F172A
)
@Composable
private fun LoadingStateDarkPreview() {
    OreoSmartOutletTheme(
        darkTheme = true
    ) {
        OreoLoadingState(
            message = "Refreshing device status",
            modifier = Modifier.padding(
                OreoSpacing.ScreenMargin
            )
        )
    }
}