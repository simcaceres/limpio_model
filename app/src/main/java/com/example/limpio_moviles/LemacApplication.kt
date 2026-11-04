package com.example.limpio_moviles

import android.app.Application
import com.example.limpio_moviles.data.local.LemacDatabase
import com.example.limpio_moviles.data.remote.FakeLemacApi
import com.example.limpio_moviles.data.repository.PerfilRepository
import com.example.limpio_moviles.data.repository.RegistroRepository
import com.example.limpio_moviles.util.NotificacionHelper

class LemacApplication : Application() {

    val database: LemacDatabase by lazy { LemacDatabase.getDatabase(this) }

    val registroRepository: RegistroRepository by lazy {
        RegistroRepository(database.registroDao(), FakeLemacApi())
    }

    val perfilRepository: PerfilRepository by lazy { PerfilRepository(this) }

    override fun onCreate() {
        super.onCreate()
        NotificacionHelper.crearCanal(this)
    }
}
