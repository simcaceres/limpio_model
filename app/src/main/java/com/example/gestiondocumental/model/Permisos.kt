package com.example.gestiondocumental.model

/**
 * Matriz de permisos (RBAC) del bloque 1 y 2 del documento Respuestas_MVP.
 *
 * La decisión de acceso combina: perfil + dependencia + tipo de documento +
 * función autorizada. Es lógica pura para poder probarla sin Android.
 *
 * Criterio del PRD: denegar por defecto. Sólo se concede acceso cuando una
 * regla explícita lo permite; los perfiles de plataforma y consulta agregada
 * no obtienen acceso automático al contenido de los expedientes.
 */
object Permisos {

    /** Categorías consideradas sensibles (licencias médicas y antecedentes). */
    fun esSensible(categoria: CategoriaDocumento): Boolean =
        categoria == CategoriaDocumento.LICENCIA_MEDICA ||
            categoria == CategoriaDocumento.ANTECEDENTE_SENSIBLE

    /**
     * ¿El rol puede VER este documento?
     *
     * @param propio el documento pertenece al usuario de la sesión.
     * @param mismaDependencia el documento está en la misma dependencia del usuario.
     */
    fun puedeVerDocumento(
        rol: Rol,
        categoria: CategoriaDocumento,
        propio: Boolean,
        mismaDependencia: Boolean
    ): Boolean {
        if (esSensible(categoria)) {
            return when (rol) {
                Rol.FUNCIONARIO -> propio          // sus propios antecedentes, acceso controlado
                Rol.RRHH -> true                   // personal autorizado
                else -> false                      // Jefatura/Administrativo/Admin/TI/Supervisor: no por defecto
            }
        }
        return when (rol) {
            Rol.FUNCIONARIO -> propio
            Rol.JEFATURA -> mismaDependencia
            Rol.ADMINISTRATIVO -> mismaDependencia
            Rol.RRHH -> true
            Rol.ADMIN_SISTEMA -> false             // administra plataforma, no contenido
            Rol.TI_CIBERSEGURIDAD -> false         // acceso excepcional no modelado
            Rol.SUPERVISOR -> false                // reportes agregados, no expedientes individuales
        }
    }

    /**
     * ¿El rol puede CARGAR un documento en la categoría indicada?
     *
     * @param propio el documento pertenece al usuario de la sesión.
     * @param mismaDependencia el documento está en la misma dependencia del usuario.
     */
    fun puedeCargarDocumento(
        rol: Rol,
        categoria: CategoriaDocumento,
        propio: Boolean,
        mismaDependencia: Boolean
    ): Boolean {
        if (rol == Rol.TI_CIBERSEGURIDAD || rol == Rol.SUPERVISOR) return false
        if (esSensible(categoria)) return rol == Rol.RRHH
        return when (rol) {
            Rol.FUNCIONARIO -> propio
            Rol.JEFATURA -> mismaDependencia
            Rol.ADMINISTRATIVO -> mismaDependencia
            Rol.RRHH -> true
            Rol.ADMIN_SISTEMA -> false
            else -> false
        }
    }

    /**
     * ¿El rol puede MODIFICAR un documento en la categoría indicada?
     *
     * TI/Ciberseguridad y Supervisor nunca modifican. Para el resto se exige
     * además que el rol pueda ver el documento en su contexto actual.
     */
    fun puedeModificarDocumento(
        rol: Rol,
        categoria: CategoriaDocumento,
        propio: Boolean,
        mismaDependencia: Boolean
    ): Boolean =
        rol != Rol.TI_CIBERSEGURIDAD && rol != Rol.SUPERVISOR &&
            puedeVerDocumento(rol, categoria, propio, mismaDependencia)

    /**
     * ¿El rol puede resolver (aprobar o rechazar) una solicitud de modificación?
     *
     * @param mismaDependencia el documento está en la misma dependencia del revisor.
     */
    fun puedeResolverSolicitud(
        rol: Rol,
        categoria: CategoriaDocumento,
        mismaDependencia: Boolean
    ): Boolean = when (rol) {
        Rol.RRHH -> true
        Rol.JEFATURA -> mismaDependencia && !esSensible(categoria)
        else -> false
    }

    /** ¿Puede consultar reportes agregados? */
    fun puedeVerReportes(rol: Rol): Boolean =
        rol == Rol.RRHH || rol == Rol.JEFATURA || rol == Rol.SUPERVISOR

    /** ¿Puede consultar reportes con detalle de expedientes individuales? */
    fun puedeVerReportesIndividuales(rol: Rol): Boolean =
        rol == Rol.RRHH || rol == Rol.JEFATURA

    /** ¿Puede crear cuentas de usuario? Sólo RR.HH. y el administrador del sistema. */
    fun puedeGestionarUsuarios(rol: Rol): Boolean =
        rol == Rol.RRHH || rol == Rol.ADMIN_SISTEMA

    /**
     * ¿El perfil pertenece a los roles que pueden resolver solicitudes?
     *
     * Puerta de rol usada por la navegación/UI. La autorización efectiva sobre
     * cada solicitud se valida con [puedeResolverSolicitud] (categoría y
     * dependencia incluidas).
     */
    fun puedeAprobar(rol: Rol): Boolean =
        rol == Rol.RRHH || rol == Rol.JEFATURA

    /** ¿Puede consultar la auditoría? */
    fun puedeAuditar(rol: Rol): Boolean =
        rol == Rol.RRHH || rol == Rol.ADMIN_SISTEMA || rol == Rol.TI_CIBERSEGURIDAD
}
