package com.iotkin.smartoutlet.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme

private val OreoLightColorScheme = lightColorScheme(
    primary = OreoPrimary,
    onPrimary = OreoOnPrimary,
    primaryContainer = OreoPrimaryContainer,
    onPrimaryContainer = OreoOnPrimaryContainer,
    inversePrimary = OreoInversePrimary,

    secondary = OreoSecondary,
    onSecondary = OreoOnSecondary,
    secondaryContainer = OreoSecondaryContainer,
    onSecondaryContainer = OreoOnSecondaryContainer,

    tertiary = OreoTertiary,
    onTertiary = OreoOnTertiary,
    tertiaryContainer = OreoTertiaryContainer,
    onTertiaryContainer = OreoOnTertiaryContainer,

    error = OreoError,
    onError = OreoOnError,
    errorContainer = OreoErrorContainer,
    onErrorContainer = OreoOnErrorContainer,

    background = OreoBackground,
    onBackground = OreoOnBackground,

    surface = OreoSurface,
    onSurface = OreoOnSurface,
    surfaceVariant = OreoSurfaceVariant,
    onSurfaceVariant = OreoOnSurfaceVariant,

    surfaceDim = OreoSurfaceDim,
    surfaceBright = OreoSurfaceBright,
    surfaceContainerLowest = OreoSurfaceContainerLowest,
    surfaceContainerLow = OreoSurfaceContainerLow,
    surfaceContainer = OreoSurfaceContainer,
    surfaceContainerHigh = OreoSurfaceContainerHigh,
    surfaceContainerHighest = OreoSurfaceContainerHighest,

    outline = OreoOutline,
    outlineVariant = OreoOutlineVariant,
    surfaceTint = OreoSurfaceTint,

    inverseSurface = OreoInverseSurface,
    inverseOnSurface = OreoInverseOnSurface,

    primaryFixed = OreoPrimaryFixed,
    primaryFixedDim = OreoPrimaryFixedDim,
    onPrimaryFixed = OreoOnPrimaryFixed,
    onPrimaryFixedVariant = OreoOnPrimaryFixedVariant,

    secondaryFixed = OreoSecondaryFixed,
    secondaryFixedDim = OreoSecondaryFixedDim,
    onSecondaryFixed = OreoOnSecondaryFixed,
    onSecondaryFixedVariant = OreoOnSecondaryFixedVariant,

    tertiaryFixed = OreoTertiaryFixed,
    tertiaryFixedDim = OreoTertiaryFixedDim,
    onTertiaryFixed = OreoOnTertiaryFixed,
    onTertiaryFixedVariant = OreoOnTertiaryFixedVariant,

    scrim = OreoScrim
)
private val OreoDarkColorScheme = darkColorScheme(
    primary = OreoDarkPrimary,
    onPrimary = OreoDarkOnPrimary,
    primaryContainer = OreoDarkPrimaryContainer,
    onPrimaryContainer = OreoDarkOnPrimaryContainer,

    secondary = OreoDarkSecondary,
    onSecondary = OreoDarkOnSecondary,
    secondaryContainer = OreoDarkSecondaryContainer,
    onSecondaryContainer = OreoDarkOnSecondaryContainer,

    tertiary = OreoDarkTertiary,
    onTertiary = OreoDarkOnTertiary,
    tertiaryContainer = OreoDarkTertiaryContainer,
    onTertiaryContainer = OreoDarkOnTertiaryContainer,

    error = OreoDarkError,
    onError = OreoDarkOnError,
    errorContainer = OreoDarkErrorContainer,
    onErrorContainer = OreoDarkOnErrorContainer,

    background = OreoDarkBackground,
    onBackground = OreoDarkOnBackground,

    surface = OreoDarkSurface,
    onSurface = OreoDarkOnSurface,
    surfaceVariant = OreoDarkSurfaceVariant,
    onSurfaceVariant = OreoDarkOnSurfaceVariant,

    surfaceDim = OreoDarkSurfaceDim,
    surfaceBright = OreoDarkSurfaceBright,
    surfaceContainerLowest = OreoDarkSurfaceContainerLowest,
    surfaceContainerLow = OreoDarkSurfaceContainerLow,
    surfaceContainer = OreoDarkSurfaceContainer,
    surfaceContainerHigh = OreoDarkSurfaceContainerHigh,
    surfaceContainerHighest = OreoDarkSurfaceContainerHighest,

    outline = OreoDarkOutline,
    outlineVariant = OreoDarkOutlineVariant,

    inverseSurface = OreoDarkInverseSurface,
    inverseOnSurface = OreoDarkInverseOnSurface
)

@Composable
fun OreoSmartOutletTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) {
            OreoDarkColorScheme
        } else {
            OreoLightColorScheme
        },
        typography = Typography,
        shapes = OreoShapes,
        content = content
    )
}