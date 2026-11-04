package com.example.limpio_moviles.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EsquemaClaro = lightColorScheme(
    primary = VerdeLemac,
    onPrimary = Color.White,
    primaryContainer = VerdeClaro,
    onPrimaryContainer = TextoOscuro,
    secondary = AzulLemac,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCE7F3),
    onSecondaryContainer = TextoOscuro,
    tertiary = NaranjaLemac,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF6E2DB),
    onTertiaryContainer = TextoOscuro,
    background = ArenaLemac,
    onBackground = TextoOscuro,
    surface = Color.White,
    onSurface = TextoOscuro,
    surfaceVariant = Color(0xFFE3EAE7),
    onSurfaceVariant = GrisTexto,
    outline = Color(0xFF8A9690),
    outlineVariant = Color(0xFFC9D2CE),
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B)
)

private val EsquemaOscuro = darkColorScheme(
    primary = VerdeClaro,
    onPrimary = TextoOscuro,
    primaryContainer = VerdeLemac,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF9DBBE0),
    onSecondary = TextoOscuro,
    secondaryContainer = Color(0xFF2A3B4E),
    onSecondaryContainer = Color(0xFFDCE7F3),
    tertiary = Color(0xFFE3A38E),
    onTertiary = TextoOscuro,
    tertiaryContainer = Color(0xFF5A3A30),
    onTertiaryContainer = Color(0xFFF6E2DB),
    background = SuperficieOscura,
    onBackground = Color(0xFFE6ECE9),
    surface = Color(0xFF1A211E),
    onSurface = Color(0xFFE6ECE9),
    surfaceVariant = Color(0xFF26302C),
    onSurfaceVariant = Color(0xFFB7C3BE),
    outline = Color(0xFF7C8983),
    outlineVariant = Color(0xFF3A4540),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC)
)

@Composable
fun LemacTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
        typography = Typography,
        content = content
    )
}
