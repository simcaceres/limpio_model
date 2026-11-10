package com.example.gestiondocumental.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey val id: Long,
    val rut: String,
    val nombre: String,
    val correo: String,
    val rol: String,
    val dependencia: String,
    val tipoContrato: String,
    val activo: Boolean,
    // Clave de demostración (en producción: hash + backend). No se expone en el modelo.
    val clave: String
)

@Entity(tableName = "documentos")
data class DocumentoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuarioId: Long,
    val propietario: String,
    val categoria: String,
    val titulo: String,
    val estado: String,
    val versionActual: Int,
    val fechaVencimiento: String?,
    val dependencia: String,
    // Nombre del archivo adjunto copiado al almacenamiento de la app (null si no hay adjunto).
    val archivoNombre: String? = null
)

@Entity(tableName = "versiones")
data class VersionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val documentoId: Long,
    val numeroVersion: Int,
    val autor: String,
    val fecha: String,
    val comentario: String
)

@Entity(tableName = "solicitudes")
data class SolicitudEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val documentoId: Long,
    val documentoTitulo: String,
    val solicitante: String,
    val tipoCambio: String,
    val datosPropuestos: String,
    val estado: String,
    val revisor: String?,
    val motivoRechazo: String?,
    val fecha: String,
    // Observación del revisor (opcional).
    val observacion: String? = null
)

@Entity(tableName = "auditoria")
data class AuditoriaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuario: String,
    val fechaHora: String,
    val entidad: String,
    val accion: String,
    val detalle: String,
    val resultado: String
)

@Entity(tableName = "dependencias")
data class DependenciaEntity(
    @PrimaryKey val id: Long,
    val nombre: String
)
