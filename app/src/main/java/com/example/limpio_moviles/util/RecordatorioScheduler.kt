package com.example.limpio_moviles.util

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

object RecordatorioScheduler {

    private const val TRABAJO_UNICO = "recordatorio_diario_lemac"
    private const val HORA_RECORDATORIO = 20

    fun programar(context: Context) {
        val ahora = Calendar.getInstance()
        val proximo = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, HORA_RECORDATORIO)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(ahora)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        val retrasoInicial = proximo.timeInMillis - ahora.timeInMillis

        val solicitud = PeriodicWorkRequestBuilder<RecordatorioWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(retrasoInicial, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            TRABAJO_UNICO,
            ExistingPeriodicWorkPolicy.UPDATE,
            solicitud
        )
    }

    fun cancelar(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(TRABAJO_UNICO)
    }
}
