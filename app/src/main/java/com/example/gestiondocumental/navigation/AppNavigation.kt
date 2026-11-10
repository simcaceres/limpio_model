package com.example.gestiondocumental.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gestiondocumental.ui.screens.AuditoriaScreen
import com.example.gestiondocumental.ui.screens.BandejaAprobacionScreen
import com.example.gestiondocumental.ui.screens.DetalleDocumentoScreen
import com.example.gestiondocumental.ui.screens.DocumentosScreen
import com.example.gestiondocumental.ui.screens.HomeScreen
import com.example.gestiondocumental.ui.screens.LoginScreen
import com.example.gestiondocumental.ui.screens.ReportesScreen
import com.example.gestiondocumental.ui.screens.SplashScreen
import com.example.gestiondocumental.ui.screens.UsuariosScreen
import com.example.gestiondocumental.viewmodel.AuditoriaViewModel
import com.example.gestiondocumental.viewmodel.DocumentosViewModel
import com.example.gestiondocumental.viewmodel.SesionViewModel
import com.example.gestiondocumental.viewmodel.SimpleFactory
import com.example.gestiondocumental.viewmodel.UsuariosViewModel

object Rutas {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val HOME = "home"
    const val DOCUMENTOS = "documentos"
    const val DETALLE = "detalle"
    const val BANDEJA = "bandeja"
    const val AUDITORIA = "auditoria"
    const val USUARIOS = "usuarios"
    const val REPORTES = "reportes"
}

@Composable
fun GestionApp() {
    val contexto = LocalContext.current
    val app = contexto.applicationContext as com.example.limpio_moviles.LemacApplication

    val sesionVm: SesionViewModel = viewModel(
        factory = SimpleFactory { SesionViewModel(app.sesionRepository) }
    )
    val docsVm: DocumentosViewModel = viewModel(
        factory = SimpleFactory {
            DocumentosViewModel(
                sesion = app.sesionRepository,
                repo = app.documentoRepository,
                usuarioRepo = app.usuarioRepository,
                appContext = contexto.applicationContext
            )
        }
    )
    val auditoriaVm: AuditoriaViewModel = viewModel(
        factory = SimpleFactory {
            AuditoriaViewModel(app.sesionRepository, app.auditoriaRepository)
        }
    )
    val usuariosVm: UsuariosViewModel = viewModel(
        factory = SimpleFactory {
            UsuariosViewModel(app.usuarioRepository, app.sesionRepository)
        }
    )

    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Rutas.SPLASH) {
        composable(Rutas.SPLASH) {
            SplashScreen(onTerminar = {
                val destino = if (sesionVm.actual.value != null) Rutas.HOME else Rutas.LOGIN
                nav.navigate(destino) {
                    popUpTo(Rutas.SPLASH) { inclusive = true }
                }
            })
        }

        composable(Rutas.LOGIN) {
            LoginScreen(
                vm = sesionVm,
                onIngresado = {
                    nav.navigate(Rutas.HOME) {
                        popUpTo(Rutas.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Rutas.HOME) {
            HomeScreen(
                sesionVm = sesionVm,
                docsVm = docsVm,
                onDocumentos = { nav.navigate(Rutas.DOCUMENTOS) },
                onBandeja = { nav.navigate(Rutas.BANDEJA) },
                onAuditoria = { nav.navigate(Rutas.AUDITORIA) },
                onUsuarios = { nav.navigate(Rutas.USUARIOS) },
                onReportes = { nav.navigate(Rutas.REPORTES) },
                onSalir = {
                    nav.navigate(Rutas.LOGIN) {
                        popUpTo(Rutas.HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Rutas.DOCUMENTOS) {
            DocumentosScreen(
                vm = docsVm,
                onAbrir = { documento ->
                    docsVm.seleccionar(documento)
                    nav.navigate(Rutas.DETALLE)
                },
                onVolver = { nav.popBackStack() }
            )
        }

        composable(Rutas.DETALLE) {
            DetalleDocumentoScreen(
                vm = docsVm,
                onVolver = { nav.popBackStack() }
            )
        }

        composable(Rutas.BANDEJA) {
            BandejaAprobacionScreen(
                vm = docsVm,
                onVolver = { nav.popBackStack() }
            )
        }

        composable(Rutas.AUDITORIA) {
            AuditoriaScreen(
                vm = auditoriaVm,
                onVolver = { nav.popBackStack() }
            )
        }

        composable(Rutas.USUARIOS) {
            UsuariosScreen(
                vm = usuariosVm,
                onVolver = { nav.popBackStack() }
            )
        }

        composable(Rutas.REPORTES) {
            ReportesScreen(
                vm = docsVm,
                onVolver = { nav.popBackStack() }
            )
        }
    }
}
