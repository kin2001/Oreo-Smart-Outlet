package com.iotkin.smartoutlet.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
fun OreoPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(
            min = OreoSpacing.MinimumTouchTarget
        ),
        enabled = enabled && !isLoading,
        shape = OreoShapeTokens.Medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme
                .surfaceContainerHighest,
            disabledContentColor = MaterialTheme.colorScheme
                .onSurfaceVariant
        )
    ) {
        OreoButtonContent(
            text = text,
            isLoading = isLoading,
            leadingIcon = leadingIcon,
            progressColor = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
fun OreoSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(
            min = OreoSpacing.MinimumTouchTarget
        ),
        enabled = enabled && !isLoading,
        shape = OreoShapeTokens.Medium,
        border = BorderStroke(
            width = 1.dp,
            color = if (enabled) {
                MaterialTheme.colorScheme.outlineVariant
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(
                    alpha = 0.45f
                )
            }
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary,
            disabledContentColor = MaterialTheme.colorScheme
                .onSurfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        OreoButtonContent(
            text = text,
            isLoading = isLoading,
            leadingIcon = leadingIcon,
            progressColor = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun OreoButtonContent(
    text: String,
    isLoading: Boolean,
    leadingIcon: (@Composable () -> Unit)?,
    progressColor: androidx.compose.ui.graphics.Color
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = progressColor
                )

                Spacer(
                    modifier = Modifier.width(
                        OreoSpacing.StackSmall
                    )
                )
            }

            leadingIcon != null -> {
                leadingIcon()

                Spacer(
                    modifier = Modifier.width(
                        OreoSpacing.StackSmall
                    )
                )
            }
        }

        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Preview(
    name = "Oreo Buttons Light",
    showBackground = true
)
@Composable
private fun OreoButtonsLightPreview() {
    OreoSmartOutletTheme(
        darkTheme = false
    ) {
        Column(
            modifier = Modifier.padding(
                OreoSpacing.ScreenMargin
            ),
            verticalArrangement = Arrangement.spacedBy(
                OreoSpacing.StackMedium
            )
        ) {
            OreoPrimaryButton(
                text = "Save Device",
                onClick = {}
            )

            OreoSecondaryButton(
                text = "Test Connection",
                onClick = {}
            )

            OreoPrimaryButton(
                text = "Connecting",
                onClick = {},
                isLoading = true
            )
        }
    }
}

@Preview(
    name = "Oreo Buttons Dark",
    showBackground = true,
    backgroundColor = 0xFF0F172A
)
@Composable
private fun OreoButtonsDarkPreview() {
    OreoSmartOutletTheme(
        darkTheme = true
    ) {
        Column(
            modifier = Modifier.padding(
                OreoSpacing.ScreenMargin
            ),
            verticalArrangement = Arrangement.spacedBy(
                OreoSpacing.StackMedium
            )
        ) {
            OreoPrimaryButton(
                text = "Edit Schedule",
                onClick = {}
            )

            OreoSecondaryButton(
                text = "Refresh Status",
                onClick = {}
            )
        }
    }
}