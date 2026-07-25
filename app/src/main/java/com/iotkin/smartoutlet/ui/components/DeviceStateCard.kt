package com.iotkin.smartoutlet.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
fun OreoOfflineState(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    message: String = "Unable to reach the smart outlet. Check that the device and phone are connected to the same network."
) {
    DeviceStateCard(
        title = "Outlet Offline",
        message = message,
        accentColor = MaterialTheme.colorScheme.error,
        modifier = modifier
    ) {
        OreoPrimaryButton(
            text = "Try Again",
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun OreoStaleState(
    lastUpdatedText: String,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val warningColor = Color(0xFFF59E0B)

    DeviceStateCard(
        title = "Status May Be Outdated",
        message = "Last updated $lastUpdatedText. Refresh to request the latest outlet state.",
        accentColor = warningColor,
        modifier = modifier
    ) {
        OreoSecondaryButton(
            text = "Refresh Status",
            onClick = onRefresh,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun DeviceStateCard(
    title: String,
    message: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    actionContent: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = OreoShapeTokens.ExtraLarge,
        color = accentColor.copy(alpha = 0.08f),
        border = BorderStroke(
            width = 1.dp,
            color = accentColor.copy(alpha = 0.28f)
        )
    ) {
        Column(
            modifier = Modifier.padding(
                OreoSpacing.CardPadding
            ),
            verticalArrangement = Arrangement.spacedBy(
                OreoSpacing.StackMedium
            )
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(
                    OreoSpacing.StackMedium
                ),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .size(10.dp)
                        .background(
                            color = accentColor,
                            shape = CircleShape
                        )
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(
                        OreoSpacing.StackSmall
                    )
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            actionContent()
        }
    }
}

@Preview(
    name = "Offline State Light",
    showBackground = true
)
@Composable
private fun OfflineStateLightPreview() {
    OreoSmartOutletTheme(
        darkTheme = false
    ) {
        Box(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.background
                )
                .padding(OreoSpacing.ScreenMargin)
        ) {
            OreoOfflineState(
                onRetry = {}
            )
        }
    }
}

@Preview(
    name = "Stale State Light",
    showBackground = true
)
@Composable
private fun StaleStateLightPreview() {
    OreoSmartOutletTheme(
        darkTheme = false
    ) {
        Box(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.background
                )
                .padding(OreoSpacing.ScreenMargin)
        ) {
            OreoStaleState(
                lastUpdatedText = "3 minutes ago",
                onRefresh = {}
            )
        }
    }
}

@Preview(
    name = "Offline State Dark",
    showBackground = true,
    backgroundColor = 0xFF0F172A
)
@Composable
private fun OfflineStateDarkPreview() {
    OreoSmartOutletTheme(
        darkTheme = true
    ) {
        Box(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.background
                )
                .padding(OreoSpacing.ScreenMargin)
        ) {
            OreoOfflineState(
                onRetry = {}
            )
        }
    }
}

@Preview(
    name = "Stale State Dark",
    showBackground = true,
    backgroundColor = 0xFF0F172A
)
@Composable
private fun StaleStateDarkPreview() {
    OreoSmartOutletTheme(
        darkTheme = true
    ) {
        Box(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.background
                )
                .padding(OreoSpacing.ScreenMargin)
        ) {
            OreoStaleState(
                lastUpdatedText = "5 minutes ago",
                onRefresh = {}
            )
        }
    }
}