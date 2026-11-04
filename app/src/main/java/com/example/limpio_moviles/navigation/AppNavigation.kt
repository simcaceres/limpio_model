package com.example.limpio_moviles.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.limpio_moviles.LemacApplication
import com.example.limpio_moviles.model.Ambito
import com.example.limpio_moviles.ui.screens.AjustesScreen
import com.example.limpio_moviles.ui.screens.HistorialScreen
import com.example.limpio_moviles.ui.screens.HomeScreen
import com.example.limpio_moviles.ui.screens.OnboardingScreen
import com.example.limpio_moviles.ui.screens.RegistroScreen
import com.example.limpio_moviles.ui.screens.ResumenScreen
import com.example.limpio_moviles.viewmodel.RegistroViewModel
import com.example.limpio_moviles.viewmodel.RegistroViewModelFactory

object Rutas {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val HISTORIAL = "historial"
    const val RESUMEN = "resumen"
    const val AJUSTES = "ajustes"
    const val REGISTRO_PATRON = "registro/{ambito}?id={id}"

    fun registro(ambito: Ambito, id: Int? = null) =
        "registro/${ambito.name}?id=${id ?: -1}"
}

@Composable
fun LemacApp() {
    val contexto = LocalContext.current
    val app = contexto.applicationContext as LemacApplication

    val factory = remember {
        RegistroViewModelFactory(
            app,
            app.registroRepository,
            app.perfilRepository
        )
    }

    val viewModel: RegistroViewModel = viewModel(factory = factory)
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = if (viewModel.perfil.alias.isBlank()) {
            Rutas.ONBOARDING
        } else {
            Rutas.HOME
        }
    ) {
        composable(route = Rutas.ONBOARDING) {
            OnboardingScreen(
                viewModel = viewModel,
                onFinalizar = {
                    navController.navigate(Rutas.HOME) {
                        popUpTo(Rutas.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Rutas.HOME) {
            HomeScreen(
                onAmbito = { ambito ->
                    navController.navigate(Rutas.registro(ambito))
                },
                onHistorial = { navController.navigate(Rutas.HISTORIAL) },
                onResumen = { navController.navigate(Rutas.RESUMEN) },
                onAjustes = { navController.navigate(Rutas.AJUSTES) },
                viewModel = viewModel
            )
        }

        composable(
            route = Rutas.REGISTRO_PATRON,
            arguments = listOf(
                navArgument("ambito") { type = NavType.StringType },
                navArgument("id") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { entrada ->
            val ambito = Ambito.desdeNombre(entrada.arguments?.getString("ambito"))
            val id = entrada.arguments?.getInt("id")?.takeIf { it > 0 }

            RegistroScreen(
                ambito = ambito,
                id = id,
                viewModel = viewModel,
                onVolver = { navController.popBackStack() }
            )
        }

        composable(route = Rutas.HISTORIAL) {
            HistorialScreen(
                viewModel = viewModel,
                onEditar = { registro ->
                    navController.navigate(
                        Rutas.registro(
                            Ambito.desdeNombre(registro.ambito),
                            registro.id
                        )
                    )
                },
                onVolver = { navController.popBackStack() }
            )
        }

        composable(route = Rutas.RESUMEN) {
            ResumenScreen(
                viewModel = viewModel,
                onVolver = { navController.popBackStack() }
            )
        }

        composable(route = Rutas.AJUSTES) {
            AjustesScreen(
                viewModel = viewModel,
                onVolver = { navController.popBackStack() }
            )
        }
    }
}
