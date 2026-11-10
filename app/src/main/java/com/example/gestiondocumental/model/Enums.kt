package com.example.gestiondocumental.model

/**
 * Perfiles oficiales del MVP (documento Respuestas_MVP, bloque 1).
 * Cada rol determina qué acciones y qué documentos puede ver.
 */
enum class Rol(val etiqueta: String, val descripcion: String) {
    FUNCIONARIO(
        "Funcionario",
        "Consulta sus propios documentos y solicita correcciones."
    ),
    JEFATURA(
        "Jefatura",
        "Revisa, valida y aprueba documentos de su equipo o dependencia."
    ),
    ADMINISTRATIVO(
        "Administrativo",
        "Carga, clasifica y actualiza documentos de su ámbito asignado."
    ),
    RRHH(
        "Recursos Humanos",
        "Administra expedientes, valida y aprueba antecedentes laborales."
    ),
    ADMIN_SISTEMA(
        "Administrador del sistema",
        "Gestiona cuentas, roles y configuración; sin acceso automático a contenido sensible."
    ),
    TI_CIBERSEGURIDAD(
        "TI / Ciberseguridad",
        "Acceso excepcional, autorizado y trazable para auditoría y seguridad."
    ),
    SUPERVISOR(
        "Supervisor institucional",
        "Consulta reportes, indicadores y trazabilidad de su ámbito."
    )
}

/** Estados del ciclo de vida de un documento. */
enum class EstadoDocumento(val etiqueta: String) {
    VIGENTE("Vigente"),
    PENDIENTE_REVISION("Pendiente de revisión"),
    APROBADO("Aprobado"),
    RECHAZADO("Rechazado"),
    ARCHIVADO("Archivado")
}

/** Estados de una solicitud de modificación (flujo RF12). */
enum class EstadoSolicitud(val etiqueta: String) {
    PENDIENTE("Pendiente"),
    APROBADA("Aprobada"),
    RECHAZADA("Rechazada")
}

/** Tipo de cambio: directo (se aplica al instante) o con revisión. */
enum class TipoCambio(val etiqueta: String) {
    DIRECTO("Directo (sin aprobación)"),
    CON_REVISION("Con revisión")
}

/** Categorías documentales del bloque 2 del documento de requisitos. */
enum class CategoriaDocumento(val etiqueta: String) {
    CONTRATO("Contrato y anexos"),
    CERTIFICADO("Certificado laboral"),
    PERMISO("Solicitud de permiso"),
    LICENCIA_MEDICA("Licencia médica"),
    ANTECEDENTE_SENSIBLE("Antecedente sensible"),
    REPORTE("Reporte institucional")
}

/** Acciones controladas por la matriz de permisos. */
enum class Accion(val etiqueta: String) {
    VER("Ver"),
    CARGAR("Cargar"),
    VALIDAR("Validar"),
    APROBAR("Aprobar"),
    RECHAZAR("Rechazar"),
    AUDITAR("Auditar")
}
