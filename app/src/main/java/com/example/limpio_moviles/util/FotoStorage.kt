package com.example.limpio_moviles.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream

object FotoStorage {

    private const val NOMBRE_ARCHIVO = "perfil.png"

    fun guardar(context: Context, bitmap: Bitmap): String {
        val archivo = File(context.filesDir, NOMBRE_ARCHIVO)
        FileOutputStream(archivo).use { salida ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, salida)
        }
        return archivo.absolutePath
    }

    fun cargar(path: String?): Bitmap? {
        if (path.isNullOrBlank()) return null
        val archivo = File(path)
        if (!archivo.exists()) return null
        return BitmapFactory.decodeFile(path)
    }
}
