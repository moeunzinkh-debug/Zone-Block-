package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val GameSpaceColorScheme = darkColorScheme(
    primary = NeonRed,
    onPrimary = TextPrimary,
    primaryContainer = NeonRedDim,
    onPrimaryContainer = NeonRedLight,
    secondary = CyberCyan,
    onSecondary = BgDark,
    secondaryContainer = CyberCyanDim,
    onSecondaryContainer = CyberCyan,
    tertiary = GameYellow,
    onTertiary = BgDark,
    tertiaryContainer = GameYellowDim,
    onTertiaryContainer = GameYellow,
    background = BgDark,
    onBackground = TextPrimary,
    surface = PanelDark,
    onSurface = TextPrimary,
    surfaceVariant = PanelDark2,
    onSurfaceVariant = TextSecondary,
    outline = PanelBorder
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BgDark.toArgb()
            window.navigationBarColor = BgDark.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = GameSpaceColorScheme,
        typography = Typography,
        content = content
    )
}
