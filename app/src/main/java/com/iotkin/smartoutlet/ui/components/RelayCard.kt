package com.iotkin.smartoutlet.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iotkin.smartoutlet.ui.theme.OreoShapeTokens
import com.iotkin.smartoutlet.ui.theme.OreoSmartOutletTheme
import com.iotkin.smartoutlet.ui.theme.OreoSpacing

@Composable
fun RelayCard(
    outletLabel: String,
    outletName: String,
    isOn: Boolean,
    scheduleSummary: String,
    lastUpdated: String,
    onToggle: (Boolean) -> Unit,
    onEditName: () -> Unit,
    onOpenDetails: () -> Unit,
    modifier: Modifier = Modifier,
    controlsEnabled: Boolean = true,
    isUpdating: Boolean = false,
    unavailableMessage: String? = null
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = OreoShapeTokens.ExtraLarge,
        color =
            MaterialTheme.colorScheme
                .surfaceContainerLowest,
        border = BorderStroke(
            width = 1.dp,
            color =
                MaterialTheme.colorScheme
                    .outlineVariant
                    .copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier =
                Modifier.padding(
                    OreoSpacing.CardPadding
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    OreoSpacing.StackSmall
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Text(
                    text = outletLabel.uppercase(),
                    style =
                        MaterialTheme.typography
                            .labelLarge,
                    color =
                        MaterialTheme.colorScheme
                            .primary
                )

                RelayToggle(
                    checked = isOn,
                    onCheckedChange = onToggle,
                    enabled = controlsEnabled,
                    isUpdating = isUpdating
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Text(
                    text = outletName,
                    style =
                        MaterialTheme.typography
                            .headlineMedium,
                    color =
                        MaterialTheme.colorScheme
                            .onSurface,
                    modifier =
                        Modifier.weight(1f)
                )

                IconButton(
                    onClick = onEditName,
                    modifier = Modifier.size(
                        OreoSpacing
                            .MinimumTouchTarget
                    )
                ) {
                    Icon(
                        imageVector =
                            Icons.Filled.Edit,
                        contentDescription =
                            "Rename $outletLabel"
                    )
                }
            }

            HorizontalDivider(
                color =
                    MaterialTheme.colorScheme
                        .outlineVariant
                        .copy(alpha = 0.30f)
            )

            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth()
            ) {
                val useStackedLayout =
                    maxWidth /
                            LocalDensity.current
                                .fontScale <
                            300.dp

                if (useStackedLayout) {
                    Column(
                        verticalArrangement =
                            Arrangement.spacedBy(
                                OreoSpacing.StackSmall
                            )
                    ) {
                        RelayStateSummary(
                            isOn = isOn,
                            isUpdating = isUpdating
                        )

                        RelayScheduleSummary(
                            scheduleSummary =
                                scheduleSummary,
                            lastUpdated = lastUpdated,
                            horizontalAlignment =
                                Alignment.Start,
                            textAlign =
                                TextAlign.Start
                        )
                    }
                } else {
                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.SpaceBetween,
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {
                        RelayStateSummary(
                            isOn = isOn,
                            isUpdating = isUpdating
                        )

                        RelayScheduleSummary(
                            scheduleSummary =
                                scheduleSummary,
                            lastUpdated = lastUpdated,
                            horizontalAlignment =
                                Alignment.End,
                            textAlign =
                                TextAlign.End
                        )
                    }
                }
            }

            unavailableMessage
                ?.takeIf {
                    it.isNotBlank()
                }
                ?.let { message ->
                    Text(
                        text = message,
                        style =
                            MaterialTheme.typography
                                .bodySmall,
                        color =
                            MaterialTheme.colorScheme
                                .error
                    )
                }

            TextButton(
                onClick = onOpenDetails,
                modifier =
                    Modifier.align(
                        Alignment.End
                    )
            ) {
                Text(
                    text = "Details"
                )
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
                scheduleSummary =
                    "8:00 PM – 5:00 AM",
                lastUpdated = "Updated 4:52 PM",
                onToggle = {},
                onEditName = {},
                onOpenDetails = {}
            )

            RelayCard(
                outletLabel = "Outlet 2",
                outletName = "Bedroom",
                isOn = false,
                scheduleSummary =
                    "1:42 PM – 1:43 PM",
                lastUpdated = "Updated 4:52 PM",
                onToggle = {},
                onEditName = {},
                onOpenDetails = {}
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
                scheduleSummary =
                    "8:00 PM – 5:00 AM",
                lastUpdated = "Updated 4:52 PM",
                onToggle = {},
                onEditName = {},
                onOpenDetails = {}
            )
        }
    }
}

@Composable
private fun RelayStateSummary(
    isOn: Boolean,
    isUpdating: Boolean
) {
    Column(
        verticalArrangement =
            Arrangement.spacedBy(
                OreoSpacing.Base
            )
    ) {
        Text(
            text =
                if (isUpdating) {
                    "Updating..."
                } else if (isOn) {
                    "ON"
                } else {
                    "OFF"
                },
            style =
                MaterialTheme.typography
                    .titleMedium,
            fontWeight =
                FontWeight.SemiBold,
            color =
                if (isOn) {
                    MaterialTheme.colorScheme
                        .primary
                } else {
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
                }
        )

        Text(
            text = "Current state",
            style =
                MaterialTheme.typography
                    .bodySmall,
            color =
                MaterialTheme.colorScheme
                    .onSurfaceVariant
        )
    }
}

@Composable
private fun RelayScheduleSummary(
    scheduleSummary: String,
    lastUpdated: String,
    horizontalAlignment:
    Alignment.Horizontal,
    textAlign: TextAlign
) {
    Column(
        horizontalAlignment =
            horizontalAlignment,
        verticalArrangement =
            Arrangement.spacedBy(
                OreoSpacing.Base
            )
    ) {
        Text(
            text = scheduleSummary,
            style =
                MaterialTheme.typography
                    .bodyMedium,
            textAlign = textAlign,
            color =
                MaterialTheme.colorScheme
                    .onSurface
        )

        Text(
            text = lastUpdated,
            style =
                MaterialTheme.typography
                    .bodySmall,
            textAlign = textAlign,
            color =
                MaterialTheme.colorScheme
                    .onSurfaceVariant
        )
    }
}

@Preview(
    name = "Relay Card Large Font",
    widthDp = 360,
    fontScale = 2f,
    showBackground = true,
    backgroundColor = 0xFF0F172A
)
@Composable
private fun RelayCardLargeFontPreview() {
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
                outletName = "Mosquito Repellent",
                isOn = false,
                scheduleSummary =
                    "8:00 PM – 5:00 AM",
                lastUpdated = "Updated 6:11 PM",
                onToggle = {},
                onEditName = {},
                onOpenDetails = {}
            )
        }
    }
}
