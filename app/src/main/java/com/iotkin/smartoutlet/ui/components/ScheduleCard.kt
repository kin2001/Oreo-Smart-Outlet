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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.iotkin.smartoutlet.ui.theme.OreoTextStyles

@Composable
fun ScheduleCard(
    outletLabel: String,
    outletName: String,
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    onTime: String,
    offTime: String,
    nextEvent: String,
    onEditSchedule: () -> Unit,
    modifier: Modifier = Modifier,
    iconContent: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = OreoShapeTokens.ExtraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme
                .surfaceContainerLowest
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
                horizontalArrangement = Arrangement.spacedBy(
                    OreoSpacing.StackMedium
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
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
                                    .secondaryContainer,
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
                    checked = enabled,
                    onCheckedChange = onEnabledChange
                )
            }

            Spacer(
                modifier = Modifier.height(
                    OreoSpacing.StackLarge
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme
                            .surfaceContainerLow,
                        shape = OreoShapeTokens.Large
                    )
                    .padding(OreoSpacing.StackMedium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ScheduleTimeBlock(
                    label = "ON Time",
                    time = onTime,
                    valueColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(48.dp)
                        .background(
                            MaterialTheme.colorScheme.outlineVariant.copy(
                                alpha = 0.3f
                            )
                        )
                )

                ScheduleTimeBlock(
                    label = "OFF Time",
                    time = offTime,
                    valueColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                        .weight(1f)
                        .padding(
                            start = OreoSpacing.StackMedium
                        )
                )
            }

            Spacer(
                modifier = Modifier.height(
                    OreoSpacing.StackMedium
                )
            )

            StatusRow(
                label = "Next Event",
                value = if (enabled) {
                    nextEvent
                } else {
                    "Schedule disabled"
                },
                valueColor = if (enabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                trailingContent = {
                    ScheduleEnabledBadge(
                        enabled = enabled
                    )
                }
            )

            Spacer(
                modifier = Modifier.height(
                    OreoSpacing.StackMedium
                )
            )

            Button(
                onClick = onEditSchedule,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = OreoShapeTokens.Pill,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "Edit Schedule",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun ScheduleTimeBlock(
    label: String,
    time: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
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
            text = time,
            style = MaterialTheme.typography.headlineMedium,
            color = valueColor
        )
    }
}

@Composable
private fun ScheduleEnabledBadge(
    enabled: Boolean
) {
    val color = if (enabled) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }

    Row(
        modifier = Modifier
            .background(
                color = color.copy(alpha = 0.12f),
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
                    color = color,
                    shape = CircleShape
                )
        )

        Text(
            text = if (enabled) {
                "Enabled"
            } else {
                "Disabled"
            },
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Preview(
    name = "Schedule Card Light",
    showBackground = true
)
@Composable
private fun ScheduleCardLightPreview() {
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
            ScheduleCard(
                outletLabel = "Outlet 1",
                outletName = "Living Room",
                enabled = true,
                onEnabledChange = {},
                onTime = "06:00 PM",
                offTime = "06:00 AM",
                nextEvent = "Today, 6:00 PM",
                onEditSchedule = {},
                iconContent = {
                    Text(
                        text = "1",
                        color = MaterialTheme.colorScheme
                            .onSecondaryContainer
                    )
                }
            )
        }
    }
}

@Preview(
    name = "Schedule Card Dark",
    showBackground = true,
    backgroundColor = 0xFF0F172A
)
@Composable
private fun ScheduleCardDarkPreview() {
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
            ScheduleCard(
                outletLabel = "Outlet 2",
                outletName = "Bedroom",
                enabled = false,
                onEnabledChange = {},
                onTime = "05:45 PM",
                offTime = "07:00 AM",
                nextEvent = "Today, 5:45 PM",
                onEditSchedule = {},
                iconContent = {
                    Text(
                        text = "2",
                        color = MaterialTheme.colorScheme
                            .onSecondaryContainer
                    )
                }
            )
        }
    }
}