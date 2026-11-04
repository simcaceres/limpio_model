package com.example.limpio_moviles.util

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class RecordatorioWorker(
    appContext: Context,
    params: WorkerParameters
) : Worker(appContext, params) {

    override fun doWork(): Result {
        NotificacionHelper.mostrar(applicationContext)
        return Result.success()
    }
}
