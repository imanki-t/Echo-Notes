package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Day Palette (Saturated Blazing Comic)
val ComicYellow = Color(0xFFFFDE4D)
val ComicPink = Color(0xFFFF4081)
val ComicCyan = Color(0xFF00E5FF)
val ComicGreen = Color(0xFF00E676)
val ComicPurple = Color(0xFFB5179E)
val ComicBlack = Color(0xFF000000)
val ComicBgLight = Color(0xFFFFF9E6)

// Night Palette (Retro Neon Glowing Dark)
val NeonDarkBg = Color(0xFF12002A)
val NeonCardDark = Color(0xFF210043)
val NeonPink = Color(0xFFFF1493)
val NeonCyan = Color(0xFF0DF5E3)
val NeonGreen = Color(0xFF39FF14)
val NeonYellow = Color(0xFFFFF700)
val NeonOffWhite = Color(0xFFF2E6FF)

fun Modifier.neoBrutalist(
    backgroundColor: Color,
    borderWidth: Dp = 3.5.dp,
    borderColor: Color = Color.Black,
    shadowOffset: Dp = 6.dp,
    shadowColor: Color = Color.Black,
    cornerRadius: Dp = 12.dp
): Modifier = this.drawBehind {
    // Draw rigid flat shadow first
    drawRoundRect(
        color = shadowColor,
        topLeft = Offset(shadowOffset.toPx(), shadowOffset.toPx()),
        size = Size(size.width, size.height),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
        style = Fill
    )
    // Draw background
    drawRoundRect(
        color = backgroundColor,
        topLeft = Offset.Zero,
        size = Size(size.width, size.height),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
        style = Fill
    )
    // Draw cartoon border outline
    drawRoundRect(
        color = borderColor,
        topLeft = Offset.Zero,
        size = Size(size.width, size.height),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
        style = Stroke(width = borderWidth.toPx())
    )
}.padding(end = shadowOffset, bottom = shadowOffset)
