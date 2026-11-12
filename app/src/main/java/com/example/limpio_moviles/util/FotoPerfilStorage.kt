package com.example.limpio_moviles.util

import android.content.Context
import android.graphics.Bitmap

/**
 * Acceso a la foto de perfil desacoplado de Android para poder probar los
 * ViewModels en la JVM.
 */
interface FotoPerfilStorage {

    fun cargar(path: String?): Bitmap?

    fun guardar(bitmap: Bitmap): String?
}

class FotoPerfilStorageAndroid(private val context: Context) : FotoPerfilStorage {

    override fun cargar(path: String?): Bitmap? = FotoStorage.cargar(path)

    override fun guardar(bitmap: Bitmap): String? =
        runCatching { FotoStorage.guardar(context, bitmap) }.getOrNull()
}
