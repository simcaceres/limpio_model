package com.example.limpio_moviles.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gestiondocumental.navigation.GestionApp
import com.example.gestiondocumental.ui.theme.GestionTheme
import com.example.limpio_moviles.ui.screens.PortalScreen

/**
 * Rutas del portal de la app unificada.
 */
object RutasPortal {
    const val PORTAL = "portal"
    const val BIENESTAR = "bienestar"
    const val GESTION = "gestion"
}

/**
 * Navegación raíz de la app unificada. Arranca en el portal y monta cada
 * módulo con su propio grafo de navegación interno; el módulo de gestión
 * añade su GestionTheme propio, mientras que el de bienestar hereda el
 * LemacTheme que aplica MainActivity a toda la app.
 */
@Composable
fun UnifiedApp() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = RutasPortal.PORTAL) {
        composable(RutasPortal.PORTAL) {
            PortalScreen(
                onBienestar = { nav.navigate(RutasPortal.BIENESTAR) },
                onGestion = { nav.navigate(RutasPortal.GESTION) }
            )
        }

        composable(RutasPortal.BIENESTAR) {
            // MainActivity ya envuelve toda la app en LemacTheme, por lo que
            // aquí no hace falta volver a aplicarlo.
            LemacApp()
        }

        composable(RutasPortal.GESTION) {
            GestionTheme { GestionApp() }
        }
    }
}
