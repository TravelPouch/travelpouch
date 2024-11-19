package com.github.se.travelpouch.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme =
    darkColorScheme(
        primary = Purple40,
        secondary = PurpleGrey80,
        tertiary = Pink40,
        background = Color(0xFF1C1B1F), // Dark background to contrast with lighter text
        surface =
            Color(0xFF2D2C31), // Dark surface color (slightly lighter than background to create
        // separation)
        inverseSurface =
            Color(0xFFFEFBFF), // Light color for surfaces that are in the inverse state
        onPrimary = Color.White, // Text on primary color needs to be white for contrast
        onSecondary = Color.White, // White text for secondary color
        secondaryContainer = Color(0xFFE7DDF6), // Lighter text for secondary color
        onTertiary = Color.White, // White text for tertiary color
        onBackground = Color.White, // White text on dark background
        onSurface = Color.White, // White text on dark surface
        inverseOnSurface = Color(0xFF1C1B1F)) // Inverse text color for the dark mode surfaces

private val LightColorScheme =
    lightColorScheme(
        primary = Purple40,
        secondary = PurpleGrey40,
        tertiary = Pink80,
        background = Color(0xFFFeF7FF),
        surface = Color(0xFFF0ECEF),
        inverseSurface = Color(0xFFFFFBFE),
        onPrimary = Color.White,
        onSecondary = Color.White,
        onTertiary = Color.White,
        onBackground = Color(0xFF1C1B1F),
        onSurface = Color(0xFF1C1B1F),
        inverseOnSurface = Color(0xFF1C1B1F))

@Composable
fun SampleAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
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
  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as Activity).window
      window.statusBarColor = colorScheme.primary.toArgb()
      WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
    }
  }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
