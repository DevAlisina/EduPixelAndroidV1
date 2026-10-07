package com.edupixel.school.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun glassSurfaceColor(darkTheme: Boolean = isSystemInDarkTheme()): Color {
    return if (darkTheme) {
        Color(0xD9131C31) // Deep translucent slate
    } else {
        Color(0xF2FFFFFF) // Frosted white
    }
}

@Composable
fun glassBorderBrush(darkTheme: Boolean = isSystemInDarkTheme()): Brush {
    return if (darkTheme) {
        Brush.linearGradient(
            listOf(
                Color(0x4D38BDF8), // Cyan glow
                Color(0x1A818CF8),
                Color(0x0D000000)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                Color(0x4D2563EB),
                Color(0x1A93C5FD),
                Color(0x0DFFFFFF)
            )
        )
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    elevation: Dp = 4.dp,
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val dark = isSystemInDarkTheme()
    val bgColor = glassSurfaceColor(dark)
    val border = glassBorderBrush(dark)

    val clickableModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    Box(
        modifier = clickableModifier
            .shadow(elevation, shape, clip = false)
            .clip(shape)
            .background(bgColor)
            .border(borderWidth, border, shape)
    ) {
        content()
    }
}

@Composable
fun GlassScreenContainer(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val dark = isSystemInDarkTheme()
    val bgBrush = if (dark) {
        Brush.verticalGradient(
            listOf(
                Color(0xFF0A0F1D),
                Color(0xFF0F172A),
                Color(0xFF0B132B)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color(0xFFF8FAFC),
                Color(0xFFEFF6FF),
                Color(0xFFF1F5F9)
            )
        )
    }

    Box(
        modifier = modifier
            .background(bgBrush)
    ) {
        content()
    }
}
