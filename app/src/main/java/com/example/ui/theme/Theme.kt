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

private val DarkColorScheme =
  darkColorScheme(
    primary = ArtisticPrimary,
    onPrimary = Color.White,
    primaryContainer = ArtisticPrimaryContainer,
    onPrimaryContainer = ArtisticOnPrimaryContainer,
    secondary = ArtisticSecondary,
    onSecondary = ArtisticOnSecondary,
    secondaryContainer = ArtisticSecondaryContainer,
    onSecondaryContainer = ArtisticOnSecondaryContainer,
    background = ArtisticDarkSurface,
    onBackground = ArtisticBackground,
    surface = ArtisticDarkSurface,
    onSurface = ArtisticBackground,
    surfaceVariant = ArtisticSurfaceVariant,
    onSurfaceVariant = ArtisticTextSecondary,
    outline = ArtisticOutline,
    error = ArtisticError
  )

private val LightColorScheme =
  lightColorScheme(
    primary = ArtisticPrimary,
    onPrimary = Color.White,
    primaryContainer = ArtisticPrimaryContainer,
    onPrimaryContainer = ArtisticOnPrimaryContainer,
    secondary = ArtisticSecondary,
    onSecondary = ArtisticOnSecondary,
    secondaryContainer = ArtisticSecondaryContainer,
    onSecondaryContainer = ArtisticOnSecondaryContainer,
    background = ArtisticBackground,
    onBackground = ArtisticTextPrimary,
    surface = ArtisticBackground,
    onSurface = ArtisticTextPrimary,
    surfaceVariant = ArtisticSurfaceVariant,
    onSurfaceVariant = ArtisticTextSecondary,
    outline = ArtisticOutline,
    error = ArtisticError
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Set to false so custom theme colors are applied
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme


  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
