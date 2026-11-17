package com.example.gestiondocumental.data.remote

import com.example.gestiondocumental.model.CategoriaDocumento
import com.example.gestiondocumental.model.Documento
import com.example.gestiondocumental.model.EstadoDocumento
import com.google.gson.annotations.SerializedName

/**
 * Contrato JSON del endpoint `GET /documentos`.
 *
 * Los enums viajan como texto y se validan al convertir el DTO al modelo de
 * dominio, de modo que un valor desconocido se reporta como error de datos.
 */
data class DocumentoDto(
    @SerializedName("id") val id: Long,
    @SerializedName("usuarioId") val usuarioId: Long,
    @SerializedName("propietario") val propietario: String,
    @SerializedName("categoria") val categoria: String,
    @SerializedName("titulo") val titulo: String,
    @SerializedName("estado") val estado: String,
    @SerializedName("versionActual") val versionActual: Int,
    @SerializedName("fechaVencimiento") val fechaVencimiento: String? = null,
    @SerializedName("dependencia") val dependencia: String,
    @SerializedName("archivoNombre") val archivoNombre: String? = null
)

/**
 * Convierte el DTO de red en el modelo de dominio [Documento].
 *
 * @throws IllegalArgumentException si el identificador o los textos obligatorios
 * no son validos, o si la categoria o el estado no pertenecen al dominio.
 */
fun DocumentoDto.aModelo(): Documento {
    require(id > 0) { "Identificador de documento invalido: $id" }
    require(titulo.isNotBlank()) { "El titulo del documento no puede estar vacio." }
    require(propietario.isNotBlank()) { "El propietario del documento no puede estar vacio." }

    val categoriaDominio = try {
        CategoriaDocumento.valueOf(categoria)
    } catch (_: IllegalArgumentException) {
        throw IllegalArgumentException("Categoria desconocida: $categoria")
    }

    val estadoDominio = try {
        EstadoDocumento.valueOf(estado)
    } catch (_: IllegalArgumentException) {
        throw IllegalArgumentException("Estado desconocido: $estado")
    }

    return Documento(
        id = id,
        usuarioId = usuarioId,
        propietario = propietario,
        categoria = categoriaDominio,
        titulo = titulo,
        estado = estadoDominio,
        versionActual = versionActual,
        fechaVencimiento = fechaVencimiento,
        dependencia = dependencia,
        archivoNombre = archivoNombre
    )
}
