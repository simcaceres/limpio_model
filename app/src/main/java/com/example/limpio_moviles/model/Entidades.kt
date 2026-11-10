package com.example.limpio_moviles.model

/** Usuario del sistema (modelo de dominio, sin credenciales). */
data class Usuario(
    val id: Long,
    val rut: String,
    val nombre: String,
    val correo: String,
    val rol: Rol,
    val dependencia: String,
    val tipoContrato: String,
    val activo: Boolean
)

/** Documento del expediente de un funcionario. */
data class Documento(
    val id: Long,
    val usuarioId: Long,
    val propietario: String,
    val categoria: CategoriaDocumento,
    val titulo: String,
    val estado: EstadoDocumento,
    val versionActual: Int,
    val fechaVencimiento: String?,
    val dependencia: String,
    // Nombre del archivo adjunto copiado al almacenamiento de la app (null si no hay adjunto).
    val archivoNombre: String? = null
)

/** Versión inmutable de un documento (historial). */
data class VersionDocumento(
    val id: Long,
    val documentoId: Long,
    val numeroVersion: Int,
    val autor: String,
    val fecha: String,
    val comentario: String
)

/** Solicitud de modificación (RF12). */
data class SolicitudModificacion(
    val id: Long,
    val documentoId: Long,
    val documentoTitulo: String,
    val solicitante: String,
    val tipoCambio: TipoCambio,
    val datosPropuestos: String,
    val estado: EstadoSolicitud,
    val revisor: String?,
    val motivoRechazo: String?,
    val fecha: String,
    val observacion: String? = null
)

/** Registro de auditoría append-only. */
data class RegistroAuditoria(
    val id: Long,
    val usuario: String,
    val fechaHora: String,
    val entidad: String,
    val accion: String,
    val detalle: String,
    val resultado: String
)

/** Unidad organizacional (dependencia / centro / unidad). */
data class Dependencia(
    val id: Long,
    val nombre: String
)