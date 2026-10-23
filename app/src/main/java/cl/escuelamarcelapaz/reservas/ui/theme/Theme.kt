package cl.escuelamarcelapaz.reservas.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = SchoolBlue,
    secondary = SchoolTeal,
    tertiary = SchoolGold,
    background = AppBackground,
    surface = Color.White,
    surfaceVariant = AppSurfaceVariant,
    onPrimary = Color.White,
    onBackground = AppInk,
    onSurface = AppInk
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8DC9E0),
    secondary = Color(0xFFB0CEC4),
    tertiary = Color(0xFFF1C875),
    background = Color(0xFF111A20),
    surface = Color(0xFF1A252D),
    surfaceVariant = Color(0xFF35444D),
    onPrimary = Color(0xFF07384E),
    onBackground = Color(0xFFE2EAF0),
    onSurface = Color(0xFFE2EAF0)
)

@Composable
fun ReservaEspaciosColegioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
