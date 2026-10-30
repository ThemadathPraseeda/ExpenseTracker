package com.pt.apps.expensetracker.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = Mint80,
    onPrimary = DarkOnPrimary,
    primaryContainer = EmeraldDark,
    onPrimaryContainer = Emerald80,
    secondary = EmeraldGrey80,
    onSecondary = DarkOnSecondary,
    tertiary = Mint80,
    error = StatusAlert,
    background = DarkBlueBg,
    surface = DarkSurfaceBg,
    surfaceVariant = DarkSurfaceVariantBg,
    onSurface = DarkOnSurfaceBg,
    onSurfaceVariant = DarkOnSurfaceVariantBg
)

private val LightColorScheme = lightColorScheme(
    primary = Emerald40,
    onPrimary = Color.White,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = EmeraldGrey40,
    onSecondary = Color.White,
    tertiary = Mint40,
    error = StatusAlert,
    background = LightBg,
    surface = Color.White,
    surfaceVariant = LightSurfaceVariantBg,
    onSurface = LightOnSurfaceBg,
    onSurfaceVariant = LightOnSurfaceVariantBg
)

@Composable
fun ExpenseTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}