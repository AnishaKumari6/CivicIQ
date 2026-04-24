package com.civiciq.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = TextOnColor,
    primaryContainer = NavyCard,
    onPrimaryContainer = TextPrimary,
    secondary = PurpleAccent,
    onSecondary = TextOnColor,
    secondaryContainer = Color(0xFF2D1B69),
    onSecondaryContainer = TextPrimary,
    tertiary = CyanGlow,
    onTertiary = NavyDeep,
    tertiaryContainer = Color(0xFF003544),
    onTertiaryContainer = CyanGlow,
    background = NavyDeep,
    onBackground = TextPrimary,
    surface = SurfaceDefault,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    error = ErrorColor,
    onError = TextOnColor,
    errorContainer = Color(0xFF4A1515),
    onErrorContainer = ErrorColor,
    outline = NavyBorder,
    outlineVariant = Color(0xFF2A3166),
    inverseSurface = TextPrimary,
    inverseOnSurface = NavyDeep,
    inversePrimary = RoyalBlue,
    scrim = Color(0x80000000),
    surfaceTint = ElectricBlue
)

private val LightColorScheme = lightColorScheme(
    primary = RoyalBlue,
    onPrimary = Color.White,
    secondary = PurpleAccent,
    onSecondary = Color.White,
    background = Color(0xFFF5F7FF),
    surface = Color.White,
    onSurface = NavyDeep
)

@Composable
fun CivicIQTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = NavyDeep.toArgb()
            window.navigationBarColor = NavyDeep.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}

object AppSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
}
