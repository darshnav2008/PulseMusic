package com.pulsemusic.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val ApplePink = Color(0xFFFA2D48)
val DeepBackground = Color(0xFF0A0A0D)
val GlassSurface = Color(0xFF1C1C22)
val SubtleBorder = Color(0x26FFFFFF)
val TextPrimary = Color(0xFFF5F5F7)
val TextSecondary = Color(0xFF98989F)

private val PulseDarkScheme = darkColorScheme(
    primary = ApplePink,
    background = DeepBackground,
    surface = GlassSurface,
    onPrimary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary
)

@Composable
fun PulseMusicTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PulseDarkScheme,
        content = content
    )
}
