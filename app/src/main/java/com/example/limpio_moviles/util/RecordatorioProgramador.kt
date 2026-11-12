package com.example.limpio_moviles.util

import android.content.Context

/**
 * Programa o cancela el recordatorio diario sin exponer WorkManager ni el
 * Context al ViewModel.
 */
interface RecordatorioProgramador {

    fun programar()

    fun cancelar()
}

class RecordatorioProgramadorAndroid(private val context: Context) : RecordatorioProgramador {

    override fun programar() {
        RecordatorioScheduler.programar(context)
    }

    override fun cancelar() {
        RecordatorioScheduler.cancelar(context)
    }
}
