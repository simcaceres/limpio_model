package com.example.gestiondocumental.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File

/** Nombre visible del archivo seleccionado en el selector del sistema. */
fun nombreVisible(context: Context, uri: Uri): String? = runCatching {
    context.contentResolver.query(
        uri,
        arrayOf(OpenableColumns.DISPLAY_NAME),
        null,
        null,
        null
    )?.use { cursor ->
        if (cursor.moveToFirst()) {
            val indice = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (indice >= 0) cursor.getString(indice) else null
        } else {
            null
        }
    }
}.getOrNull()

/**
 * Copia el contenido de [uri] al almacenamiento privado de la app
 * (`filesDir/documentos`) y devuelve el nombre del archivo guardado.
 * Devuelve null si no se pudo leer o escribir el archivo.
 */
fun copiarArchivo(context: Context, uri: Uri): String? = runCatching {
    val visible = nombreVisible(context, uri)?.trim().orEmpty()
    val base = visible
        .ifBlank { "documento" }
        .replace(Regex("[^\\p{L}\\p{N}._-]"), "_")
        .ifBlank { "documento" }
    val nombre = "${System.currentTimeMillis()}_$base"

    val directorio = File(context.filesDir, "documentos")
    if (!directorio.exists() && !directorio.mkdirs()) return@runCatching null

    val entrada = context.contentResolver.openInputStream(uri) ?: return@runCatching null
    entrada.use { origen ->
        File(directorio, nombre).outputStream().use { destino ->
            origen.copyTo(destino)
        }
    }
    nombre
}.getOrNull()

/** Archivo privado asociado a un documento cargado. */
fun archivoDocumento(context: Context, nombre: String): File =
    File(context.filesDir, "documentos/$nombre")

/** Tipo MIME aproximado según la extensión del archivo. */
fun mimeDe(nombre: String): String = when (nombre.substringAfterLast('.', "").lowercase()) {
    "pdf" -> "application/pdf"
    "jpg", "jpeg" -> "image/jpeg"
    "png" -> "image/png"
    "doc" -> "application/msword"
    "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    "xls" -> "application/vnd.ms-excel"
    "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    else -> "application/octet-stream"
}
