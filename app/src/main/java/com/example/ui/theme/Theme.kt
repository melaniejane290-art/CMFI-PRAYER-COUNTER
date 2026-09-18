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
    primary = SoftGold,
    onPrimary = DeepNavy,
    primaryContainer = MidnightBlue,
    onPrimaryContainer = GoldContainer,
    secondary = BrightGold,
    onSecondary = DeepNavy,
    tertiary = NavyLighter,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceCard,
    onBackground = PureWhite,
    onSurface = PureWhite,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = RoyalNavy,
    onPrimary = PureWhite,
    primaryContainer = GoldContainer,
    onPrimaryContainer = DeepNavy,
    secondary = SoftGold,
    onSecondary = DeepNavy,
    tertiary = NavyLight,
    background = OffWhite,
    surface = SurfaceCard,
    surfaceVariant = SurfaceVariantBg,
    onBackground = TextDark,
    onSurface = TextDark,
    outline = GoldOutline,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamic color by default so prayer brand palette stays cohesive
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

