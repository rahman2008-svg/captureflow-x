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

private val DarkColorScheme = darkColorScheme(
    primary = BlueVioletPrimary,
    secondary = IndigoSecondary,
    tertiary = VioletTertiary,
    background = DarkBg,
    surface = DarkSurface,
    surfaceVariant = DarkCard,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFF1F5F9), // Slate 100
    onSurface = Color(0xFFF1F5F9) // Slate 100
)

private val AmoledColorScheme = darkColorScheme(
    primary = BlueVioletPrimary,
    secondary = IndigoSecondary,
    tertiary = VioletTertiary,
    background = AmoledBg,
    surface = AmoledSurface,
    surfaceVariant = AmoledCard,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = IndigoSecondary,
    tertiary = VioletTertiary,
    background = LightBg,
    surface = LightSurface,
    surfaceVariant = LightCard,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    amoledMode: Boolean = false,
    dynamicColor: Boolean = false, // Disable dynamic color to maintain consistent brand identity
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        amoledMode -> AmoledColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
