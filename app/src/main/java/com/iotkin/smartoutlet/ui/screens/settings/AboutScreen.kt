@file:Suppress("DEPRECATION")

package com.iotkin.smartoutlet.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.iotkin.smartoutlet.ui.theme.OreoShapeTokens
import com.iotkin.smartoutlet.ui.theme.OreoSpacing

@Composable
fun AboutScreen(
    firmwareVersion: String?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appVersion = remember(context) {
        val packageInfo =
            context.packageManager
                .getPackageInfo(
                    context.packageName,
                    0
                )

        "${packageInfo.versionName ?: "Unknown"} (build ${packageInfo.versionCode})"
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = OreoSpacing.ScreenMargin,
            vertical = OreoSpacing.StackMedium
        ),
        verticalArrangement =
            Arrangement.spacedBy(
                OreoSpacing.StackMedium
            )
    ) {
        item {
            TextButton(onClick = onBack) {
                Text(text = "Back")
            }
        }

        item {
            Column(
                verticalArrangement =
                    Arrangement.spacedBy(
                        OreoSpacing.StackSmall
                    )
            ) {
                Text(
                    text = "About",
                    style =
                        MaterialTheme.typography
                            .headlineLarge
                )

                Text(
                    text = "Oreo Smart Outlet",
                    style =
                        MaterialTheme.typography
                            .bodyLarge,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )
            }
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
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
                    modifier = Modifier.padding(
                        OreoSpacing.CardPadding
                    ),
                    verticalArrangement =
                        Arrangement.spacedBy(
                            OreoSpacing.StackMedium
                        )
                ) {
                    AboutValue(
                        label = "App version",
                        value = appVersion
                    )

                    HorizontalDivider(
                        color =
                            MaterialTheme.colorScheme
                                .outlineVariant
                                .copy(alpha = 0.35f)
                    )

                    AboutValue(
                        label = "Firmware version",
                        value =
                            firmwareVersion
                                ?: "Unavailable"
                    )
                }
            }
        }

        if (firmwareVersion == null) {
            item {
                Text(
                    text =
                        "The firmware version appears after the app receives a status response from the saved outlet.",
                    style =
                        MaterialTheme.typography
                            .bodyMedium,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AboutValue(
    label: String,
    value: String
) {
    Column(
        verticalArrangement =
            Arrangement.spacedBy(
                OreoSpacing.Base
            )
    ) {
        Text(
            text = label,
            style =
                MaterialTheme.typography
                    .labelLarge,
            color =
                MaterialTheme.colorScheme
                    .onSurfaceVariant
        )

        Text(
            text = value,
            style =
                MaterialTheme.typography
                    .bodyLarge
        )
    }
}
