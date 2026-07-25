package com.iotkin.smartoutlet.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/*
 * Android sans-serif is the temporary fallback.
 * The exact Inter font family will be connected later without changing
 * the typography sizes or component code.
 */
private val OreoFontFamily = FontFamily.SansSerif

private val DisplayTimeStyle = TextStyle(
    fontFamily = OreoFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 42.sp,
    lineHeight = 48.sp,
    letterSpacing = (-0.02).em
)

private val HeadlineLargeStyle = TextStyle(
    fontFamily = OreoFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 28.sp,
    lineHeight = 34.sp
)

private val HeadlineMediumStyle = TextStyle(
    fontFamily = OreoFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 20.sp,
    lineHeight = 26.sp
)

private val BodyLargeStyle = TextStyle(
    fontFamily = OreoFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 24.sp
)

private val BodyMediumStyle = TextStyle(
    fontFamily = OreoFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp
)

private val LabelCapsStyle = TextStyle(
    fontFamily = OreoFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 11.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.08.em
)

private val LabelSmallStyle = TextStyle(
    fontFamily = OreoFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp
)

val Typography = Typography(
    displayLarge = DisplayTimeStyle,
    headlineLarge = HeadlineLargeStyle,
    headlineMedium = HeadlineMediumStyle,
    bodyLarge = BodyLargeStyle,
    bodyMedium = BodyMediumStyle,
    labelMedium = LabelCapsStyle,
    labelSmall = LabelSmallStyle
)

object OreoTextStyles {
    val DisplayTime = DisplayTimeStyle
    val LabelCaps = LabelCapsStyle
}