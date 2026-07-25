package com.iotkin.smartoutlet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iotkin.smartoutlet.ui.theme.OreoShapeTokens
import com.iotkin.smartoutlet.ui.theme.OreoSmartOutletTheme
import com.iotkin.smartoutlet.ui.theme.OreoSpacing

enum class ConnectionStatus {
    Online,
    Reconnecting,
    Offline
}

@Composable
fun ConnectionBadge(
    status: ConnectionStatus,
    modifier: Modifier = Modifier
) {
    val statusColor = connectionStatusColor(status)
    val statusText = connectionStatusText(status)

    Row(
        modifier = modifier
            .background(
                color = statusColor.copy(alpha = 0.12f),
                shape = OreoShapeTokens.Pill
            )
            .border(
                width = 1.dp,
                color = statusColor.copy(alpha = 0.28f),
                shape = OreoShapeTokens.Pill
            )
            .padding(
                horizontal = OreoSpacing.StackMedium,
                vertical = OreoSpacing.StackSmall
            ),
        horizontalArrangement = Arrangement.spacedBy(
            OreoSpacing.StackSmall
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = statusColor,
                    shape = CircleShape
                )
        )

        Text(
            text = statusText,
            color = statusColor,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun connectionStatusColor(
    status: ConnectionStatus
): Color {
    return when (status) {
        ConnectionStatus.Online ->
            MaterialTheme.colorScheme.primary

        ConnectionStatus.Reconnecting ->
            Color(0xFFF59E0B)

        ConnectionStatus.Offline ->
            MaterialTheme.colorScheme.error
    }
}

private fun connectionStatusText(
    status: ConnectionStatus
): String {
    return when (status) {
        ConnectionStatus.Online -> "Online"
        ConnectionStatus.Reconnecting -> "Reconnecting"
        ConnectionStatus.Offline -> "Offline"
    }
}

@Preview(
    name = "Connection Badges Light",
    showBackground = true
)
@Composable
private fun ConnectionBadgeLightPreview() {
    OreoSmartOutletTheme(
        darkTheme = false
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ConnectionBadge(
                status = ConnectionStatus.Online
            )

            ConnectionBadge(
                status = ConnectionStatus.Offline
            )
        }
    }
}

@Preview(
    name = "Connection Badge Dark",
    showBackground = true,
    backgroundColor = 0xFF0F172A
)
@Composable
private fun ConnectionBadgeDarkPreview() {
    OreoSmartOutletTheme(
        darkTheme = true
    ) {
        ConnectionBadge(
            status = ConnectionStatus.Reconnecting,
            modifier = Modifier.padding(16.dp)
        )
    }
}