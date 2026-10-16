package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val Mt5ColorScheme = darkColorScheme(
  primary = Mt5BidBlue,
  onPrimary = Mt5DarkBackground,
  primaryContainer = Mt5DarkSurfaceElevated,
  onPrimaryContainer = Mt5TextPrimary,
  secondary = Mt5BullGreen,
  onSecondary = Mt5DarkBackground,
  secondaryContainer = Mt5DarkSurface,
  onSecondaryContainer = Mt5TextPrimary,
  tertiary = Mt5Amber,
  background = Mt5DarkBackground,
  onBackground = Mt5TextPrimary,
  surface = Mt5DarkSurface,
  onSurface = Mt5TextPrimary,
  surfaceVariant = Mt5DarkSurfaceElevated,
  onSurfaceVariant = Mt5TextSecondary,
  outline = Mt5Border,
  error = Mt5BearRed,
  onError = Mt5TextPrimary
)

val Mt5Shapes = Shapes(
  extraSmall = RoundedCornerShape(6.dp),
  small = RoundedCornerShape(10.dp),
  medium = RoundedCornerShape(14.dp),
  large = RoundedCornerShape(18.dp),
  extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = Mt5ColorScheme,
    typography = Typography,
    shapes = Mt5Shapes,
    content = content
  )
}
