package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun HorizonLauncherTheme(
    accentColor: Color = BrightCyan,
    surfaceTint: Color = MidnightCobalt,
    content: @Composable () -> Unit,
) {
    val colorScheme = darkColorScheme(
        primary = accentColor,
        onPrimary = DeepSpaceBlack,
        primaryContainer = surfaceTint,
        onPrimaryContainer = PureWhite,
        secondary = LuminousIce,
        onSecondary = DeepSpaceBlack,
        secondaryContainer = GlassBlue,
        onSecondaryContainer = PureWhite,
        tertiary = EmeraldGlow,
        background = DeepSpaceBlack,
        onBackground = PureWhite,
        surface = MidnightCobalt,
        onSurface = PureWhite,
        surfaceVariant = GlassSurface,
        onSurfaceVariant = MutedSlate,
        outline = accentColor.copy(alpha = 0.45f),
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
