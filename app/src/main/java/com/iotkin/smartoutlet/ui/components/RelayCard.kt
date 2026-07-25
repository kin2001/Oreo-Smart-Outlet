package com.iotkin.smartoutlet.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iotkin.smartoutlet.ui.theme.OreoShapeTokens
import com.iotkin.smartoutlet.ui.theme.OreoSmartOutletTheme
import com.iotkin.smartoutlet.ui.theme.OreoSpacing
import com.iotkin.smartoutlet.ui.theme.OreoTextStyles

@Composable
fun RelayCard(
    outletLabel: String,
    outletName: String,
    isOn: Boolean,
    nextEvent: String,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    iconContent: @Composable () -> Unit
) {
    val statusColor = if (isOn) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = OreoShapeTokens.ExtraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(
                alpha = 0.3f
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(
                OreoSpacing.CardPadding
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(
                        OreoSpacing.StackMedium
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(OreoSpacing.RelayIconContainer)
                            .background(
                                color = MaterialTheme.colorScheme
                                    .surfaceContainerHigh,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        iconContent()
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(
                            OreoSpacing.Base
                        )
                    ) {
                        Text(
                            text = outletLabel.uppercase(),
                            style = OreoTextStyles.LabelCaps,
                            color = MaterialTheme.colorScheme
                                .onSurfaceVariant
                        )

                        Text(
                            text = outletName,
                            style = MaterialTheme.typography
                                .headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                RelayToggle(
                    checked = isOn,
                    onCheckedChange = onToggle
                )
            }

            Spacer(
                modifier = Modifier.height(
                    OreoSpacing.StackLarge
                )
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        MaterialTheme.colorScheme.outlineVariant.copy(
                            alpha = 0.2f
                        )
                    )
            )

            Spacer(
                modifier = Modifier.height(
                    OreoSpacing.StackMedium
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(
                        OreoSpacing.Base
                    )
                ) {
                    Row(
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
                            text = if (isOn) "ON" else "OFF",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "Current State",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme
                            .onSurfaceVariant
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(
                        OreoSpacing.Base
                    )
                ) {
                    Text(
                        text = nextEvent,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Next Event",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme
                            .onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(
    name = "Relay Cards Light",
    showBackground = true
)
@Composable
private fun RelayCardLightPreview() {
    OreoSmartOutletTheme(
        darkTheme = false
    ) {
        Column(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.background
                )
                .padding(OreoSpacing.ScreenMargin),
            verticalArrangement = Arrangement.spacedBy(
                OreoSpacing.StackMedium
            )
        ) {
            RelayCard(
                outletLabel = "Outlet 1",
                outletName = "Living Room",
                isOn = true,
                nextEvent = "Today, 6:00 PM",
                onToggle = {},
                iconContent = {
                    Text(
                        text = "1",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            )

            RelayCard(
                outletLabel = "Outlet 2",
                outletName = "Bedroom",
                isOn = false,
                nextEvent = "Today, 5:45 PM",
                onToggle = {},
                iconContent = {
                    Text(
                        text = "2",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        }
    }
}

@Preview(
    name = "Relay Card Dark",
    showBackground = true,
    backgroundColor = 0xFF0F172A
)
@Composable
private fun RelayCardDarkPreview() {
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
            RelayCard(
                outletLabel = "Outlet 1",
                outletName = "Living Room",
                isOn = true,
                nextEvent = "Tomorrow, 6:00 AM",
                onToggle = {},
                iconContent = {
                    Text(
                        text = "1",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            )
        }
    }
}