package com.batal.elyoum.ui.theme

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
  primary = HeroGold,
  onPrimary = Color.Black,
  primaryContainer = NavyCard,
  onPrimaryContainer = HeroGoldLight,
  secondary = HeroBlueLight,
  onSecondary = Color.White,
  secondaryContainer = NavySurface,
  onSecondaryContainer = HeroBlueContainer,
  tertiary = HeroGreen,
  onTertiary = Color.White,
  background = BackgroundDark,
  onBackground = TextPrimaryDark,
  surface = SurfaceDark,
  onSurface = TextPrimaryDark,
  surfaceVariant = SurfaceVariantDark,
  onSurfaceVariant = TextSecondaryDark,
  outline = Color(0xFF374151),
  outlineVariant = Color(0xFF1F2937)
)

private val LightColorScheme = lightColorScheme(
  primary = HeroGoldDark,
  onPrimary = Color.White,
  primaryContainer = HeroGoldContainer,
  onPrimaryContainer = Color(0xFF78350F),
  secondary = HeroBlue,
  onSecondary = Color.White,
  secondaryContainer = HeroBlueContainer,
  onSecondaryContainer = Color(0xFF1E3A8A),
  tertiary = HeroGreen,
  onTertiary = Color.White,
  tertiaryContainer = HeroGreenContainer,
  onTertiaryContainer = Color(0xFF065F46),
  background = BackgroundLight,
  onBackground = TextPrimaryLight,
  surface = SurfaceLight,
  onSurface = TextPrimaryLight,
  surfaceVariant = SurfaceVariantLight,
  onSurfaceVariant = TextSecondaryLight,
  outline = Color(0xFFE2E8F0),
  outlineVariant = Color(0xFFCBD5E1)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // We prefer our curated heroic theme colors for strong brand identity
  dynamicColor: Boolean = false,
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
