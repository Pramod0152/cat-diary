package com.petwell.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SoftTeal = Color(0xFF26A69A)
private val SoftTealDark = Color(0xFF00796B)
private val SoftTealLight = Color(0xFFB2DFDB)
private val SurfaceTint = Color(0xFFE0F2F1)

private val SoftBlue = Color(0xFF5C6BC0)
private val SoftBlueDark = Color(0xFF3F51B5)
private val SoftBlueLight = Color(0xFFC5CAE9)

private val WarmAmber = Color(0xFFFFB74D)
private val WarmAmberLight = Color(0xFFFFE0B2)

private val SurfaceLight = Color(0xFFF8FAFB)
private val SurfaceDark = Color(0xFF1C1B1F)

private val PetWellLightColors = lightColorScheme(
    primary = SoftTeal,
    onPrimary = Color.White,
    primaryContainer = SoftTealLight,
    onPrimaryContainer = SoftTealDark,

    secondary = SoftBlue,
    onSecondary = Color.White,
    secondaryContainer = SoftBlueLight,
    onSecondaryContainer = SoftBlueDark,

    tertiary = WarmAmber,
    onTertiary = Color(0xFF3E2723),
    tertiaryContainer = WarmAmberLight,
    onTertiaryContainer = Color(0xFF4E342E),

    background = SurfaceLight,
    onBackground = Color(0xFF1C1B1F),
    surface = Color.White,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE8EDF0),
    onSurfaceVariant = Color(0xFF49454F),
    surfaceTint = SurfaceTint,

    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFC4C7C9),

    error = Color(0xFFE57373),
    onError = Color.White,
    errorContainer = Color(0xFFFFCDD2),
    onErrorContainer = Color(0xFFC62828)
)

private val PetWellDarkColors = darkColorScheme(
    primary = Color(0xFF80CBC4),
    onPrimary = Color(0xFF00332E),
    primaryContainer = SoftTealDark,
    onPrimaryContainer = SoftTealLight,

    secondary = Color(0xFF9FA8DA),
    onSecondary = Color(0xFF1A237E),
    secondaryContainer = SoftBlueDark,
    onSecondaryContainer = SoftBlueLight,

    tertiary = Color(0xFFFFCC80),
    onTertiary = Color(0xFF4E342E),
    tertiaryContainer = Color(0xFF6D4C41),
    onTertiaryContainer = WarmAmberLight,

    background = SurfaceDark,
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF25232A),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF2E2C33),
    onSurfaceVariant = Color(0xFFCAC4D0),
    surfaceTint = Color(0xFF26A69A),

    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F),

    error = Color(0xFFEF9A9A),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFCDD2)
)

@Composable
fun PetWellTheme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) PetWellDarkColors else PetWellLightColors

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
