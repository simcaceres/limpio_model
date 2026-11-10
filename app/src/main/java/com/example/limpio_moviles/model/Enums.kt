package com.example.limpio_moviles.model

enum class Rol(val etiqueta: String, val descripcion: String) {
    FUNCIONARIO(
        etiqueta = "Funcionario",
        descripcion = "Consulta sus propios documentos y solicita correcciones."
    ),
    JEFATURA(
        etiqueta = "Jefatura",
        descripcion = "Revisa, valida y aprueba documentos de su equipo o dependencia."
    ),
    ADMINISTRATIVO(
        etiqueta = "Administrativo",
        descripcion = "Carga, clasifica y actualiza documentos de su ámbito asignado."
    ),
    RRHH(
        etiqueta = "Recursos Humanos",
        descripcion = "Administra expedientes, valida y aprueba antecedentes laborales."
    ),
    ADMIN_SISTEMA(
        etiqueta = "Administrador del sistema",
        descripcion = "Gestiona cuentas, roles y configuración; sin acceso automático a contenido sensible."
    ),
    TI_CIBERSEGURIDAD(
        etiqueta = "TI / Ciberseguridad",
        descripcion = "Acceso excepcional, autorizado y trazable para auditoría y seguridad."
    ),
    SUPERVISOR(
        etiqueta = "Supervisor institucional",
        descripcion = "Consulta reportes, indicadores y trazabilidad de su ámbito."
    )
}

/** Estados del ciclo de vida de un documento. */
enum class EstadoDocumento(val etiqueta: String) {
    VIGENTE(etiqueta = "Vigente"),
    PENDIENTE_REVISION(etiqueta = "Pendiente de revisión"),
    APROBADO(etiqueta = "Aprobado"),
    RECHAZADO(etiqueta = "Rechazado"),
    ARCHIVADO(etiqueta = "Archivado")
}

/** Estados de una solicitud de modificación (flujo RF12). */
enum class EstadoSolicitud(val etiqueta: String) {
    PENDIENTE(etiqueta = "Pendiente"),
    APROBADA(etiqueta = "Aprobada"),
    RECHAZADA(etiqueta = "Rechazada")
}

/** Tipo de cambio: directo (se aplica al instante) o con revisión. */
enum class TipoCambio(val etiqueta: String) {
    DIRECTO(etiqueta = "Directo (sin aprobación)"),
    CON_REVISION(etiqueta = "Con revisión")
}

/** Categorías documentales. */
enum class CategoriaDocumento(val etiqueta: String) {
    CONTRATO(etiqueta = "Contrato y anexos"),
    CERTIFICADO(etiqueta = "Certificado laboral"),
    PERMISO(etiqueta = "Solicitud de permiso"),
    LICENCIA_MEDICA(etiqueta = "Licencia médica"),
    ANTECEDENTE_SENSIBLE(etiqueta = "Antecedente sensible"),
    REPORTE(etiqueta = "Reporte institucional")
}

/** Acciones controladas por la matriz de permisos. */
enum class Accion(val etiqueta: String) {
    VER(etiqueta = "Ver"),
    CARGAR(etiqueta = "Cargar"),
    VALIDAR(etiqueta = "Validar"),
    APROBAR(etiqueta = "Aprobar"),
    RECHAZAR(etiqueta = "Rechazar"),
    AUDITAR(etiqueta = "Auditar")
}