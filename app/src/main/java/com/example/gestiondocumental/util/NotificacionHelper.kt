package com.example.gestiondocumental.util

import android.annotation.SuppressLint
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/**
 * Notificaciones locales de la app (vencimientos y resolución de solicitudes).
 *
 * El canal se crea una vez al arrancar la aplicación. En Android 13+ el envío
 * sólo se realiza si el usuario otorgó el permiso POST_NOTIFICATIONS.
 */
object NotificacionHelper {

    const val CANAL_ID = "gestion_documental"

    /** Crea el canal de notificaciones en Android 8.0+ (idempotente). */
    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val canal = NotificationChannel(
            CANAL_ID,
            "Gestión documental",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Avisos de vencimientos y solicitudes"
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(canal)
    }

    /** ¿La app puede publicar notificaciones en este dispositivo? */
    fun puedeNotificar(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    /** Publica una notificación simple; no hace nada si falta el permiso. */
    @SuppressLint("MissingPermission")
    fun notificar(context: Context, id: Int, titulo: String, texto: String) {
        if (!puedeNotificar(context)) return
        crearCanal(context)
        val notificacion = NotificationCompat.Builder(context, CANAL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_more)
            .setContentTitle(titulo)
            .setContentText(texto)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(id, notificacion)
    }
}
