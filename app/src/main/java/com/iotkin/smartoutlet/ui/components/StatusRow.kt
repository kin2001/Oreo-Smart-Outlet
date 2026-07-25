package com.iotkin.smartoutlet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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

@Composable
fun StatusRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = OreoSpacing.StackSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingContent != null) {
            Box(
                modifier = Modifier.size(
                    OreoSpacing.StandardIcon
                ),
                contentAlignment = Alignment.Center
            ) {
                leadingContent()
            }

            Spacer(
                modifier = Modifier.width(
                    OreoSpacing.StackMedium
                )
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(
                OreoSpacing.Base
            )
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = valueColor
            )
        }

        if (trailingContent != null) {
            Spacer(
                modifier = Modifier.width(
                    OreoSpacing.StackMedium
                )
            )

            trailingContent()
        }
    }
}

@Composable
private fun ReadyBadge() {
    Row(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.primary.copy(
                    alpha = 0.12f
                ),
                shape = OreoShapeTokens.Pill
            )
            .padding(
                horizontal = OreoSpacing.StackSmall,
                vertical = OreoSpacing.Base
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
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
        )

        Text(
            text = "Ready",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Preview(
    name = "Status Row Light",
    showBackground = true
)
@Composable
private fun StatusRowLightPreview() {
    OreoSmartOutletTheme(
        darkTheme = false
    ) {
        StatusRow(
            label = "Wi-Fi Signal",
            value = "-55 dBm",
            modifier = Modifier.padding(
                OreoSpacing.ScreenMargin
            )
        )
    }
}

@Preview(
    name = "Status Row Dark",
    showBackground = true,
    backgroundColor = 0xFF0F172A
)
@Composable
private fun StatusRowDarkPreview() {
    OreoSmartOutletTheme(
        darkTheme = true
    ) {
        StatusRow(
            label = "API Status",
            value = "Device responding",
            modifier = Modifier.padding(
                OreoSpacing.ScreenMargin
            ),
            trailingContent = {
                ReadyBadge()
            }
        )
    }
}