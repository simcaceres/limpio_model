package com.example.limpio_moviles.util

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Escritura de contenido en un Uri elegido por el usuario. Aísla el
 * ContentResolver del ViewModel para que este sea testeable en la JVM.
 */
interface ExportadorArchivo {

    suspend fun escribir(uri: Uri, contenido: String): Boolean
}

class ExportadorArchivoAndroid(private val context: Context) : ExportadorArchivo {

    override suspend fun escribir(uri: Uri, contenido: String): Boolean =
        withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { salida ->
                    salida.write(contenido.toByteArray(Charsets.UTF_8))
                } != null
            }.getOrDefault(false)
        }
}
