package com.example.limpio_moviles.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object FotoStorage {

    private const val NOMBRE_ARCHIVO = "perfil.png"
    private const val NOMBRE_ARCHIVO_TEMPORAL = "perfil.png.tmp"
    private const val CARPETA_TEMPORAL = "fotos"
    private const val PREFIJO_TEMPORAL = "captura_"
    private const val SUFIJO_TEMPORAL = ".jpg"
    private const val LADO_MAXIMO_PX = 1024
    private const val EDAD_MAXIMA_TEMPORAL_MS = 24 * 60 * 60 * 1000L

    fun crearArchivoTemporal(context: Context): File {
        val carpeta = File(context.cacheDir, CARPETA_TEMPORAL)
        if (!carpeta.exists() && !carpeta.mkdirs()) {
            error("No se pudo crear la carpeta temporal de fotos")
        }
        limpiarTemporalesAntiguos(carpeta)
        return File.createTempFile(PREFIJO_TEMPORAL, SUFIJO_TEMPORAL, carpeta)
    }

    private fun limpiarTemporalesAntiguos(carpeta: File) {
        val limite = System.currentTimeMillis() - EDAD_MAXIMA_TEMPORAL_MS
        carpeta.listFiles()?.forEach { archivo ->
            if (archivo.isFile && archivo.lastModified() < limite) {
                archivo.delete()
            }
        }
    }

    fun uriParaArchivo(context: Context, archivo: File): Uri =
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            archivo
        )

    fun guardar(context: Context, bitmap: Bitmap): String {
        val archivo = File(context.filesDir, NOMBRE_ARCHIVO)
        val temporal = File(context.filesDir, NOMBRE_ARCHIVO_TEMPORAL)
        try {
            FileOutputStream(temporal).use { salida ->
                check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, salida)) {
                    "No se pudo comprimir la foto de perfil"
                }
            }
            if (!temporal.renameTo(archivo)) {
                temporal.copyTo(archivo, overwrite = true)
            }
            return archivo.absolutePath
        } finally {
            if (temporal.exists()) temporal.delete()
        }
    }

    fun cargar(path: String?): Bitmap? {
        if (path.isNullOrBlank()) return null
        val archivo = File(path)
        if (!archivo.exists()) return null

        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, limites)
        if (limites.outWidth <= 0 || limites.outHeight <= 0) return null

        val opciones = BitmapFactory.Options().apply {
            inSampleSize = calcularMuestra(limites.outWidth, limites.outHeight)
        }
        return BitmapFactory.decodeFile(path, opciones)
    }

    fun cargarDesdeUri(context: Context, uri: Uri): Bitmap? {
        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { entrada ->
            BitmapFactory.decodeStream(entrada, null, limites)
        }
        if (limites.outWidth <= 0 || limites.outHeight <= 0) return null

        val opciones = BitmapFactory.Options().apply {
            inSampleSize = calcularMuestra(limites.outWidth, limites.outHeight)
        }
        val bitmap = context.contentResolver.openInputStream(uri)?.use { entrada ->
            BitmapFactory.decodeStream(entrada, null, opciones)
        } ?: return null

        return aplicarOrientacion(bitmap, leerOrientacion(context, uri))
    }

    private fun calcularMuestra(ancho: Int, alto: Int): Int {
        var muestra = 1
        while (ancho / muestra > LADO_MAXIMO_PX || alto / muestra > LADO_MAXIMO_PX) {
            muestra *= 2
        }
        return muestra
    }

    private fun leerOrientacion(context: Context, uri: Uri): Int =
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { entrada ->
                ExifInterface(entrada).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            } ?: ExifInterface.ORIENTATION_NORMAL
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

    private fun aplicarOrientacion(bitmap: Bitmap, orientacion: Int): Bitmap {
        val matriz = Matrix()
        when (orientacion) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matriz.setScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matriz.setRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matriz.setScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matriz.setRotate(90f)
                matriz.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_90 -> matriz.setRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matriz.setRotate(-90f)
                matriz.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> matriz.setRotate(-90f)
            else -> return bitmap
        }

        val orientado = Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matriz,
            true
        )
        if (orientado !== bitmap) bitmap.recycle()
        return orientado
    }
}
