package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = ElegantBlue,
    secondary = ElegantPurple,
    tertiary = ElegantPurpleLight,
    background = ElegantDarkBg,
    surface = ElegantDarkCard,
    onBackground = ElegantDarkSlateText,
    onSurface = ElegantDarkSlateText,
    outline = ElegantDarkBorder
  )

private val LightColorScheme =
  darkColorScheme( // Enforce Elegant Dark on all systems for true brand consistency!
    primary = ElegantBlue,
    secondary = ElegantPurple,
    tertiary = ElegantBlueLight,
    background = ElegantDarkBg,
    surface = ElegantDarkCard,
    onBackground = ElegantDarkSlateText,
    onSurface = ElegantDarkSlateText,
    outline = ElegantDarkBorder
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color can be disabled to enforce our custom luxury brand style
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
