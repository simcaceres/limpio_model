package com.example.gestiondocumental.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EsquemaClaro = lightColorScheme(
    primary = AzulInstitucional,
    onPrimary = Color.White,
    primaryContainer = AzulClaro,
    onPrimaryContainer = TextoOscuro,
    secondary = AzulAcento,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCEAF6),
    onSecondaryContainer = TextoOscuro,
    tertiary = VerdeOk,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFDDF2DF),
    onTertiaryContainer = TextoOscuro,
    background = FondoClaro,
    onBackground = TextoOscuro,
    surface = Color.White,
    onSurface = TextoOscuro,
    surfaceVariant = Color(0xFFE6EDF4),
    onSurfaceVariant = GrisTexto,
    outline = Color(0xFF8A97A3),
    outlineVariant = Color(0xFFC9D3DC),
    error = RojoError,
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B)
)

private val EsquemaOscuro = darkColorScheme(
    primary = AzulClaro,
    onPrimary = TextoOscuro,
    primaryContainer = AzulInstitucional,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF9DBBE0),
    onSecondary = TextoOscuro,
    secondaryContainer = Color(0xFF2A3B4E),
    onSecondaryContainer = Color(0xFFDCEAF6),
    tertiary = Color(0xFF9BD69F),
    onTertiary = TextoOscuro,
    tertiaryContainer = Color(0xFF1E4620),
    onTertiaryContainer = Color(0xFFDDF2DF),
    background = SuperficieOscura,
    onBackground = Color(0xFFE6ECF1),
    surface = Color(0xFF1A222B),
    onSurface = Color(0xFFE6ECF1),
    surfaceVariant = Color(0xFF26313C),
    onSurfaceVariant = Color(0xFFB7C2CC),
    outline = Color(0xFF7C8994),
    outlineVariant = Color(0xFF3A454F),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC)
)

@Composable
fun GestionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
        typography = Typography,
        content = content
    )
}
