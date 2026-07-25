package com.iotkin.smartoutlet.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iotkin.smartoutlet.ui.theme.OreoShapeTokens
import com.iotkin.smartoutlet.ui.theme.OreoSmartOutletTheme

@Composable
fun RelayToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isUpdating: Boolean = false
) {
    val trackColor by animateColorAsState(
        targetValue = if (checked) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest
        },
        label = "RelayToggleTrackColor"
    )

    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 26.dp else 2.dp,
        label = "RelayToggleThumbOffset"
    )

    Box(
        modifier = modifier
            .size(
                width = 52.dp,
                height = 48.dp
            )
            .toggleable(
                value = checked,
                enabled = enabled && !isUpdating,
                role = Role.Switch,
                onValueChange = onCheckedChange
            )
            .alpha(
                if (enabled) 1f else 0.45f
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(
                    width = 52.dp,
                    height = 28.dp
                )
                .background(
                    color = trackColor,
                    shape = OreoShapeTokens.Pill
                )
        ) {
            Box(
                modifier = Modifier
                    .offset(x = thumbOffset)
                    .align(Alignment.CenterStart)
                    .size(24.dp)
                    .background(
                        color = Color.White,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Preview(
    name = "Relay Toggle On",
    showBackground = true
)
@Composable
private fun RelayToggleOnPreview() {
    OreoSmartOutletTheme(
        darkTheme = false
    ) {
        Box(
            modifier = Modifier.size(
                width = 220.dp,
                height = 80.dp
            ),
            contentAlignment = Alignment.Center
        ) {
            RelayToggle(
                checked = true,
                onCheckedChange = {}
            )
        }
    }
}

@Preview(
    name = "Relay Toggle Off",
    showBackground = true
)
@Composable
private fun RelayToggleOffPreview() {
    OreoSmartOutletTheme(
        darkTheme = false
    ) {
        Box(
            modifier = Modifier.size(
                width = 220.dp,
                height = 80.dp
            ),
            contentAlignment = Alignment.Center
        ) {
            RelayToggle(
                checked = false,
                onCheckedChange = {}
            )
        }
    }
}

@Preview(
    name = "Relay Toggle Updating Dark",
    showBackground = true,
    backgroundColor = 0xFF0F172A
)
@Composable
private fun RelayToggleUpdatingPreview() {
    OreoSmartOutletTheme(
        darkTheme = true
    ) {
        Box(
            modifier = Modifier.size(
                width = 220.dp,
                height = 80.dp
            ),
            contentAlignment = Alignment.Center
        ) {
            RelayToggle(
                checked = true,
                isUpdating = true,
                onCheckedChange = {}
            )
        }
    }
}