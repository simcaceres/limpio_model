package com.example.limpio_moviles

import android.app.Application
import com.example.gestiondocumental.data.local.GestionDatabase
import com.example.gestiondocumental.data.local.Sembrador
import com.example.gestiondocumental.data.repository.AuditoriaRepository
import com.example.gestiondocumental.data.repository.DocumentoRepository
import com.example.gestiondocumental.data.repository.SesionRepository
import com.example.gestiondocumental.data.repository.UsuarioRepository
import com.example.limpio_moviles.data.local.LemacDatabase
import com.example.limpio_moviles.data.remote.FakeLemacApi
import com.example.limpio_moviles.data.repository.PerfilRepository
import com.example.limpio_moviles.data.repository.RegistroRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Contenedor de dependencias manual de la app unificada.
 *
 * Expone la base Room y los repositorios del módulo de bienestar
 * (Lemac DataLab) y del módulo de Gestión Documental RR.HH. También
 * crea los canales de notificación de ambos módulos y siembra los
 * datos de demostración de gestión documental la primera vez que se
 * abre la app.
 */
class LemacApplication : Application() {

    private val alcance = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Módulo Bienestar — Lemac DataLab
    val database: LemacDatabase by lazy { LemacDatabase.getDatabase(this) }

    val registroRepository: RegistroRepository by lazy {
        RegistroRepository(database.registroDao(), FakeLemacApi())
    }

    val perfilRepository: PerfilRepository by lazy { PerfilRepository(this) }

    // Módulo Gestión Documental RR.HH.
    val gestionDatabase: GestionDatabase by lazy { GestionDatabase.obtener(this) }

    val sesionRepository: SesionRepository by lazy {
        SesionRepository(gestionDatabase.usuarioDao(), gestionDatabase.auditoriaDao())
    }

    val documentoRepository: DocumentoRepository by lazy {
        DocumentoRepository(gestionDatabase)
    }

    val usuarioRepository: UsuarioRepository by lazy {
        UsuarioRepository(gestionDatabase)
    }

    val auditoriaRepository: AuditoriaRepository by lazy {
        AuditoriaRepository(gestionDatabase.auditoriaDao())
    }

    override fun onCreate() {
        super.onCreate()
        com.example.limpio_moviles.util.NotificacionHelper.crearCanal(this)
        com.example.gestiondocumental.util.NotificacionHelper.crearCanal(this)
        alcance.launch { Sembrador.sembrarSiVacio(gestionDatabase) }
    }
}
