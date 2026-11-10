package com.example.gestiondocumental.model

/**
 * Reglas del requisito RF12: modificación de información y trazabilidad.
 *
 * Idea clave: no basta con "editar". Hay cambios que se aplican directo
 * (campos habilitados) y otros que quedan pendientes de revisión. Mientras
 * una modificación está pendiente, la versión anterior sigue vigente.
 */
object FlujoModificacion {

    /** Determina si una categoría requiere revisión o se aplica directo. */
    fun tipoCambioPara(categoria: CategoriaDocumento): TipoCambio = when (categoria) {
        CategoriaDocumento.CONTRATO,
        CategoriaDocumento.CERTIFICADO,
        CategoriaDocumento.LICENCIA_MEDICA,
        CategoriaDocumento.ANTECEDENTE_SENSIBLE -> TipoCambio.CON_REVISION

        CategoriaDocumento.PERMISO,
        CategoriaDocumento.REPORTE -> TipoCambio.DIRECTO
    }

    /** ¿El rol puede aprobar o rechazar una solicitud de modificación? */
    fun puedeResolver(rol: Rol): Boolean = Permisos.puedeAprobar(rol)

    /** Número de la siguiente versión al aprobar una modificación. */
    fun siguienteVersion(versionActual: Int): Int = versionActual + 1

    /** Estado resultante del documento cuando se aprueba una modificación. */
    fun estadoTrasAprobacion(): EstadoDocumento = EstadoDocumento.VIGENTE

    /** Estado resultante de una solicitud rechazada (el documento no cambia). */
    fun estadoSolicitudRechazada(): EstadoSolicitud = EstadoSolicitud.RECHAZADA

    /** Texto de auditoría homogéneo para una modificación directa. */
    fun detalleDirecto(datos: String): String = "Edición directa aplicada: $datos"

    /** Texto de auditoría homogéneo para una solicitud con revisión. */
    fun detalleSolicitud(datos: String): String = "Solicitud de modificación con revisión: $datos"

    /** Texto de auditoría homogéneo para un rechazo. */
    fun detalleRechazo(motivo: String): String = "Solicitud rechazada. Motivo: $motivo"
}
