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

private val SleekDarkColorScheme =
  darkColorScheme(
    primary = Indigo500,
    secondary = Sky400,
    tertiary = Emerald500,
    background = Slate900,
    surface = Slate800,
    surfaceVariant = Slate700,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Slate300
  )

private val SleekLightColorScheme =
  lightColorScheme(
    primary = Indigo600,
    secondary = Sky500,
    tertiary = Emerald500,
    background = SleekBg,
    surface = SleekSurface,
    surfaceVariant = SleekTileBg,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Slate900,
    onSurface = Slate900,
    onSurfaceVariant = Slate600
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false,
  // Use our curated Sleek Interface colors by default
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> SleekDarkColorScheme
      else -> SleekLightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

