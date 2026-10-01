package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Composable
fun HomeHelpTheme(
    brandTheme: BrandThemeOption = BrandThemeOption.INDIGO,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Keep global variables in sync for convenience
    BrandBluePrimary = brandTheme.primary
    BrandBlueDark = brandTheme.primaryDark
    BrandBlueLight = brandTheme.primaryLight
    BrandAmberAccent = brandTheme.accent

    val darkColorScheme = darkColorScheme(
        primary = brandTheme.primaryLight,
        onPrimary = Color.White,
        secondary = brandTheme.accent,
        onSecondary = Color.Black,
        tertiary = StatusOnlineGreen,
        background = SurfaceDark,
        surface = SurfaceCardDark,
        onBackground = TextPrimaryDark,
        onSurface = TextPrimaryDark,
        surfaceVariant = Color(0xFF1E293B),
        onSurfaceVariant = TextSecondaryDark,
        error = StatusErrorRed
    )

    val lightColorScheme = lightColorScheme(
        primary = brandTheme.primary,
        onPrimary = Color.White,
        secondary = brandTheme.accent,
        onSecondary = Color.White,
        tertiary = StatusOnlineGreen,
        background = SurfaceLight,
        surface = SurfaceCard,
        onBackground = TextPrimary,
        onSurface = TextPrimary,
        surfaceVariant = SurfaceCardSubtle,
        onSurfaceVariant = TextSecondary,
        outline = BorderLight,
        error = StatusErrorRed
    )

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme
        else -> lightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
