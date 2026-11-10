package com.example.gestiondocumental.data.repository

import androidx.room.withTransaction
import com.example.gestiondocumental.data.local.AuditoriaDao
import com.example.gestiondocumental.data.local.AuditoriaEntity
import com.example.gestiondocumental.data.local.DocumentoEntity
import com.example.gestiondocumental.data.local.GestionDatabase
import com.example.gestiondocumental.data.local.SolicitudEntity
import com.example.gestiondocumental.data.local.UsuarioDao
import com.example.gestiondocumental.data.local.UsuarioEntity
import com.example.gestiondocumental.data.local.VersionEntity
import com.example.gestiondocumental.data.local.aModelo
import com.example.gestiondocumental.model.CategoriaDocumento
import com.example.gestiondocumental.model.Documento
import com.example.gestiondocumental.model.EstadoDocumento
import com.example.gestiondocumental.model.EstadoSolicitud
import com.example.gestiondocumental.model.FlujoModificacion
import com.example.gestiondocumental.model.Permisos
import com.example.gestiondocumental.model.RegistroAuditoria
import com.example.gestiondocumental.model.Rol
import com.example.gestiondocumental.model.SolicitudModificacion
import com.example.gestiondocumental.model.TipoCambio
import com.example.gestiondocumental.model.Usuario
import com.example.gestiondocumental.model.Validadores
import com.example.gestiondocumental.model.VersionDocumento
import com.example.gestiondocumental.util.ahora
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * Sesión de la aplicación. Mantiene el usuario autenticado y registra el
 * inicio de sesión en la auditoría. En este MVP la autenticación es local
 * (correo + clave) sobre los usuarios sembrados en Room.
 */
class SesionRepository(
    private val usuarioDao: UsuarioDao,
    private val auditoriaDao: AuditoriaDao
) {
    private val _actual = MutableStateFlow<Usuario?>(null)
    val actual: StateFlow<Usuario?> = _actual.asStateFlow()

    suspend fun iniciarSesion(correo: String, clave: String): Result<Usuario> {
        val correoNormalizado = correo.trim().lowercase()
        val entidad = usuarioDao.buscarPorCorreo(correoNormalizado)
        if (entidad == null) {
            registrarAuditoria(
                usuario = correoNormalizado,
                entidad = "Sesion",
                accion = "LOGIN_FALLIDO",
                detalle = "No existe una cuenta con ese correo.",
                resultado = "ERROR"
            )
            return Result.failure(IllegalArgumentException("No existe una cuenta con ese correo."))
        }
        if (!entidad.activo) {
            registrarAuditoria(
                usuario = entidad.nombre,
                entidad = "Sesion",
                accion = "LOGIN_FALLIDO",
                detalle = "La cuenta está desactivada. Contacta a RR.HH.",
                resultado = "ERROR"
            )
            return Result.failure(IllegalStateException("La cuenta está desactivada. Contacta a RR.HH."))
        }
        if (entidad.clave != clave) {
            registrarAuditoria(
                usuario = entidad.nombre,
                entidad = "Sesion",
                accion = "LOGIN_FALLIDO",
                detalle = "Clave incorrecta.",
                resultado = "ERROR"
            )
            return Result.failure(IllegalArgumentException("Clave incorrecta."))
        }
        val usuario = entidad.aModelo()
        _actual.value = usuario
        registrarAuditoria(
            usuario = usuario.nombre,
            entidad = "Sesion",
            accion = "LOGIN",
            detalle = "Inicio de sesión como ${usuario.rol.etiqueta}",
            resultado = "OK"
        )
        return Result.success(usuario)
    }

    /** Cierra la sesión actual dejando registro en la auditoría antes de limpiarla. */
    suspend fun cerrarSesion() {
        _actual.value?.let { usuario ->
            registrarAuditoria(
                usuario = usuario.nombre,
                entidad = "Sesion",
                accion = "LOGOUT",
                detalle = "Cierre de sesión",
                resultado = "OK"
            )
        }
        _actual.value = null
    }

    private suspend fun registrarAuditoria(
        usuario: String,
        entidad: String,
        accion: String,
        detalle: String,
        resultado: String
    ) {
        auditoriaDao.insertar(
            AuditoriaEntity(
                usuario = usuario,
                fechaHora = ahora(),
                entidad = entidad,
                accion = accion,
                detalle = detalle,
                resultado = resultado
            )
        )
    }
}

/**
 * Documentos, versiones y solicitudes de modificación (RF12).
 * Todas las lecturas están acotadas al usuario de la sesión y toda operación
 * de escritura deja un registro en la auditoría dentro de una transacción.
 */
class DocumentoRepository(private val db: GestionDatabase) {

    private val documentoDao = db.documentoDao()
    private val versionDao = db.versionDao()
    private val solicitudDao = db.solicitudDao()
    private val auditoriaDao = db.auditoriaDao()

    /** Documentos visibles para el usuario según la matriz RBAC. */
    fun observarDocumentosPara(usuario: Usuario): Flow<List<Documento>> =
        documentoDao.observarTodos().map { lista ->
            lista.filter {
                Permisos.puedeVerDocumento(
                    usuario.rol,
                    CategoriaDocumento.valueOf(it.categoria),
                    it.usuarioId == usuario.id,
                    it.dependencia == usuario.dependencia
                )
            }.map { it.aModelo() }
        }

    /** Documentos usados para reportes agregados, acotados por rol. */
    fun observarParaReportes(usuario: Usuario): Flow<List<Documento>> {
        if (!Permisos.puedeVerReportes(usuario.rol)) return flowOf(emptyList())
        return documentoDao.observarTodos().map { lista ->
            when (usuario.rol) {
                Rol.RRHH -> lista
                Rol.JEFATURA -> lista.filter { it.dependencia == usuario.dependencia }
                Rol.SUPERVISOR -> lista
                else -> emptyList()
            }
        }.map { lista -> lista.map { it.aModelo() } }
    }

    /** Solicitudes de modificación sobre documentos visibles para el usuario. */
    fun observarSolicitudesPara(usuario: Usuario): Flow<List<SolicitudModificacion>> =
        combine(solicitudDao.observarTodas(), documentoDao.observarTodos()) { solicitudes, documentos ->
            val visibles = documentos.filter {
                Permisos.puedeVerDocumento(
                    usuario.rol,
                    CategoriaDocumento.valueOf(it.categoria),
                    it.usuarioId == usuario.id,
                    it.dependencia == usuario.dependencia
                )
            }.map { it.id }.toSet()
            solicitudes.filter { it.documentoId in visibles }.map { it.aModelo() }
        }

    /** Solicitudes pendientes que el revisor puede efectivamente resolver. */
    fun observarPendientesPara(revisor: Usuario): Flow<List<SolicitudModificacion>> =
        combine(solicitudDao.observarPendientes(), documentoDao.observarTodos()) { solicitudes, documentos ->
            val porId = documentos.associateBy { it.id }
            solicitudes.mapNotNull { s ->
                val d = porId[s.documentoId] ?: return@mapNotNull null
                if (Permisos.puedeResolverSolicitud(
                        revisor.rol,
                        CategoriaDocumento.valueOf(d.categoria),
                        d.dependencia == revisor.dependencia
                    )
                ) {
                    s.aModelo()
                } else {
                    null
                }
            }
        }

    suspend fun porId(id: Long): Documento? = documentoDao.porId(id)?.aModelo()

    suspend fun versionesDe(documentoId: Long): List<VersionDocumento> =
        versionDao.listarPorDocumento(documentoId).map { it.aModelo() }

    /**
     * Carga un nuevo documento al expediente del propietario.
     *
     * Valida permisos, título y fecha de vencimiento; crea la versión 1 y deja
     * el evento en la auditoría. Si la categoría requiere revisión, el documento
     * nace en estado PENDIENTE_REVISION.
     */
    suspend fun cargarDocumento(
        propietario: Usuario,
        categoria: CategoriaDocumento,
        titulo: String,
        fechaVencimiento: String?,
        archivoNombre: String?,
        actor: Usuario
    ): Result<Long> {
        if (titulo.isBlank()) {
            return Result.failure(IllegalArgumentException("El título es obligatorio."))
        }
        val autorizado = Permisos.puedeCargarDocumento(
            rol = actor.rol,
            categoria = categoria,
            propio = propietario.id == actor.id,
            mismaDependencia = propietario.dependencia == actor.dependencia
        )
        if (!autorizado) {
            auditar(
                usuario = actor.nombre,
                entidad = "Documento",
                accion = "CARGA",
                detalle = "Intento denegado de cargar $titulo (${categoria.etiqueta})",
                resultado = "DENEGADO"
            )
            return Result.failure(SecurityException("No tienes permisos para cargar este documento."))
        }

        val fechaNormalizada = fechaVencimiento?.trim()?.takeIf { it.isNotEmpty() }
        if (fechaNormalizada != null) {
            val formatoValido = Regex("\\d{4}-\\d{2}-\\d{2}").matches(fechaNormalizada)
            val fechaValida = runCatching { LocalDate.parse(fechaNormalizada) }.isSuccess
            if (!formatoValido || !fechaValida) {
                return Result.failure(IllegalArgumentException("La fecha de vencimiento no es válida."))
            }
        }

        return runCatching {
            db.withTransaction {
                val estado = if (FlujoModificacion.tipoCambioPara(categoria) == TipoCambio.CON_REVISION) {
                    EstadoDocumento.PENDIENTE_REVISION
                } else {
                    EstadoDocumento.VIGENTE
                }
                val tituloNormalizado = titulo.trim()
                val id = documentoDao.insertar(
                    DocumentoEntity(
                        usuarioId = propietario.id,
                        propietario = propietario.nombre,
                        categoria = categoria.name,
                        titulo = tituloNormalizado,
                        estado = estado.name,
                        versionActual = 1,
                        fechaVencimiento = fechaNormalizada,
                        dependencia = propietario.dependencia,
                        archivoNombre = archivoNombre
                    )
                )
                versionDao.insertar(
                    VersionEntity(
                        documentoId = id,
                        numeroVersion = 1,
                        autor = actor.nombre,
                        fecha = ahora(),
                        comentario = "Carga inicial"
                    )
                )
                val detalleArchivo = archivoNombre?.takeIf { it.isNotBlank() }?.let { " · archivo: $it" } ?: ""
                auditar(
                    usuario = actor.nombre,
                    entidad = "Documento",
                    accion = "CARGA",
                    detalle = "Carga de $tituloNormalizado (${categoria.etiqueta})$detalleArchivo"
                )
                id
            }
        }
    }

    /**
     * RF12. Si la categoría admite edición directa, se aplica de inmediato y se
     * crea una nueva versión. Si requiere revisión, se crea una solicitud
     * PENDIENTE y el documento actual sigue vigente sin cambios.
     */
    suspend fun solicitarModificacion(
        documento: Documento,
        solicitante: Usuario,
        datos: String
    ): Result<Unit> = runCatching {
        if (!Permisos.puedeModificarDocumento(
                rol = solicitante.rol,
                categoria = documento.categoria,
                propio = documento.usuarioId == solicitante.id,
                mismaDependencia = documento.dependencia == solicitante.dependencia
            )
        ) {
            auditar(
                usuario = solicitante.nombre,
                entidad = "Documento",
                accion = "MODIFICACION",
                detalle = "Intento denegado de modificar ${documento.titulo}",
                resultado = "DENEGADO"
            )
            throw SecurityException("No tienes permisos para modificar este documento.")
        }
        val tipo = FlujoModificacion.tipoCambioPara(documento.categoria)
        db.withTransaction {
            if (tipo == TipoCambio.DIRECTO) {
                val entidad = documentoDao.porId(documento.id)
                    ?: error("El documento ya no existe.")
                val nuevaVersion = FlujoModificacion.siguienteVersion(entidad.versionActual)
                versionDao.insertar(
                    VersionEntity(
                        documentoId = entidad.id,
                        numeroVersion = nuevaVersion,
                        autor = solicitante.nombre,
                        fecha = ahora(),
                        comentario = FlujoModificacion.detalleDirecto(datos)
                    )
                )
                documentoDao.actualizar(
                    entidad.copy(
                        versionActual = nuevaVersion,
                        estado = EstadoDocumento.VIGENTE.name
                    )
                )
                auditar(
                    usuario = solicitante.nombre,
                    entidad = "Documento",
                    accion = "MODIFICACION_DIRECTA",
                    detalle = "${documento.titulo}: ${FlujoModificacion.detalleDirecto(datos)}"
                )
            } else {
                solicitudDao.insertar(
                    SolicitudEntity(
                        documentoId = documento.id,
                        documentoTitulo = documento.titulo,
                        solicitante = solicitante.nombre,
                        tipoCambio = tipo.name,
                        datosPropuestos = datos,
                        estado = EstadoSolicitud.PENDIENTE.name,
                        revisor = null,
                        motivoRechazo = null,
                        fecha = ahora()
                    )
                )
                auditar(
                    usuario = solicitante.nombre,
                    entidad = "Documento",
                    accion = "SOLICITUD_MODIFICACION",
                    detalle = "${documento.titulo}: ${FlujoModificacion.detalleSolicitud(datos)}"
                )
            }
        }
    }

    /** Aprueba una solicitud: publica nueva versión y conserva la anterior. */
    suspend fun aprobar(solicitudId: Long, revisor: Usuario): Result<Unit> = runCatching {
        val solicitud = solicitudDao.porId(solicitudId)
            ?: error("La solicitud ya no existe.")
        val documento = documentoDao.porId(solicitud.documentoId)
            ?: error("El documento asociado no existe.")
        if (!Permisos.puedeResolverSolicitud(
                revisor.rol,
                CategoriaDocumento.valueOf(documento.categoria),
                documento.dependencia == revisor.dependencia
            )
        ) {
            auditar(
                usuario = revisor.nombre,
                entidad = "Solicitud",
                accion = "APROBAR",
                detalle = "Intento denegado sobre solicitud #$solicitudId",
                resultado = "DENEGADO"
            )
            error("No tienes permisos para resolver esta solicitud.")
        }
        db.withTransaction {
            val filas = solicitudDao.resolver(
                solicitudId,
                EstadoSolicitud.APROBADA.name,
                revisor.nombre,
                null,
                solicitud.observacion
            )
            if (filas == 0) error("La solicitud ya fue resuelta.")
            val nuevaVersion = FlujoModificacion.siguienteVersion(documento.versionActual)
            versionDao.insertar(
                VersionEntity(
                    documentoId = documento.id,
                    numeroVersion = nuevaVersion,
                    autor = revisor.nombre,
                    fecha = ahora(),
                    comentario = "Aprobada solicitud #${solicitud.id}: ${solicitud.datosPropuestos}"
                )
            )
            documentoDao.actualizar(
                documento.copy(
                    versionActual = nuevaVersion,
                    estado = FlujoModificacion.estadoTrasAprobacion().name
                )
            )
            auditar(
                usuario = revisor.nombre,
                entidad = "Solicitud",
                accion = "APROBAR",
                detalle = "Solicitud #${solicitud.id} aprobada para ${solicitud.documentoTitulo}",
                resultado = "OK"
            )
        }
    }

    /** Rechaza una solicitud: mantiene vigente la versión anterior. */
    suspend fun rechazar(solicitudId: Long, revisor: Usuario, motivo: String): Result<Unit> =
        runCatching {
            require(motivo.isNotBlank()) { "Debes indicar un motivo de rechazo." }
            val solicitud = solicitudDao.porId(solicitudId)
                ?: error("La solicitud ya no existe.")
            val documento = documentoDao.porId(solicitud.documentoId)
                ?: error("El documento asociado no existe.")
            if (!Permisos.puedeResolverSolicitud(
                    revisor.rol,
                    CategoriaDocumento.valueOf(documento.categoria),
                    documento.dependencia == revisor.dependencia
                )
            ) {
                auditar(
                    usuario = revisor.nombre,
                    entidad = "Solicitud",
                    accion = "RECHAZAR",
                    detalle = "Intento denegado sobre solicitud #$solicitudId",
                    resultado = "DENEGADO"
                )
                error("No tienes permisos para resolver esta solicitud.")
            }
            db.withTransaction {
                val filas = solicitudDao.resolver(
                    solicitudId,
                    EstadoSolicitud.RECHAZADA.name,
                    revisor.nombre,
                    motivo,
                    motivo
                )
                if (filas == 0) error("La solicitud ya fue resuelta.")
                auditar(
                    usuario = revisor.nombre,
                    entidad = "Solicitud",
                    accion = "RECHAZAR",
                    detalle = "Solicitud #${solicitud.id}: ${FlujoModificacion.detalleRechazo(motivo)}",
                    resultado = "OK"
                )
            }
        }

    private suspend fun auditar(
        usuario: String,
        entidad: String,
        accion: String,
        detalle: String,
        resultado: String = "OK"
    ) {
        auditoriaDao.insertar(
            AuditoriaEntity(
                usuario = usuario,
                fechaHora = ahora(),
                entidad = entidad,
                accion = accion,
                detalle = detalle,
                resultado = resultado
            )
        )
    }
}

/** Gestión de cuentas de usuario (creación y activación). */
class UsuarioRepository(private val db: GestionDatabase) {

    private val usuarioDao = db.usuarioDao()
    private val auditoriaDao = db.auditoriaDao()

    /** Usuarios visibles según el rol del actor. */
    fun observarPara(actor: Usuario): Flow<List<Usuario>> =
        usuarioDao.observarTodos().map { lista ->
            lista.filter {
                when (actor.rol) {
                    Rol.RRHH, Rol.ADMIN_SISTEMA -> true
                    Rol.JEFATURA, Rol.ADMINISTRATIVO -> it.dependencia == actor.dependencia
                    else -> it.id == actor.id
                }
            }.map { it.aModelo() }
        }

    suspend fun crear(
        nombre: String,
        correo: String,
        rut: String,
        rol: Rol,
        dependencia: String,
        tipoContrato: String,
        actor: Usuario
    ): Result<Unit> = runCatching {
        require(Permisos.puedeGestionarUsuarios(actor.rol)) {
            "No tienes permisos para gestionar usuarios."
        }
        val error = Validadores.usuarioValido(nombre, correo, rut)
        require(error == null) { error ?: "Datos inválidos." }
        val correoNormalizado = correo.trim().lowercase()
        val rutNormalizado = Validadores.rutCanonico(rut)
        db.withTransaction {
            require(usuarioDao.buscarPorCorreo(correoNormalizado) == null) {
                "Ya existe una cuenta con ese correo."
            }
            require(usuarioDao.buscarPorRut(rutNormalizado) == null) {
                "Ya existe una cuenta con ese RUT."
            }
            val id = usuarioDao.maxId() + 1
            usuarioDao.insertar(
                UsuarioEntity(
                    id = id,
                    rut = rutNormalizado,
                    nombre = nombre.trim(),
                    correo = correoNormalizado,
                    rol = rol.name,
                    dependencia = dependencia,
                    tipoContrato = tipoContrato,
                    activo = true,
                    clave = "1234"
                )
            )
            auditoriaDao.insertar(
                AuditoriaEntity(
                    usuario = actor.nombre,
                    fechaHora = ahora(),
                    entidad = "Usuario",
                    accion = "CREAR",
                    detalle = "Cuenta creada para $nombre (${rol.etiqueta})",
                    resultado = "OK"
                )
            )
        }
    }

    suspend fun alternarActivo(usuario: Usuario, actor: Usuario): Result<Unit> = runCatching {
        require(Permisos.puedeGestionarUsuarios(actor.rol)) {
            "No tienes permisos para gestionar usuarios."
        }
        db.withTransaction {
            val entidad = usuarioDao.porId(usuario.id)
                ?: error("El usuario ya no existe.")
            val nuevoEstado = !entidad.activo
            require(!(usuario.id == actor.id && !nuevoEstado)) {
                "No puedes desactivar tu propia cuenta."
            }
            usuarioDao.actualizar(entidad.copy(activo = nuevoEstado))
            auditoriaDao.insertar(
                AuditoriaEntity(
                    usuario = actor.nombre,
                    fechaHora = ahora(),
                    entidad = "Usuario",
                    accion = if (nuevoEstado) "ACTIVAR" else "DESACTIVAR",
                    detalle = "Cuenta de ${entidad.nombre} ${if (nuevoEstado) "activada" else "desactivada"}",
                    resultado = "OK"
                )
            )
        }
    }
}

/** Consulta de la bitácora de auditoría (append-only). */
class AuditoriaRepository(private val auditoriaDao: AuditoriaDao) {

    /** Registros visibles sólo para perfiles autorizados a auditar. */
    fun observarPara(actor: Usuario): Flow<List<RegistroAuditoria>> =
        if (!Permisos.puedeAuditar(actor.rol)) {
            flowOf(emptyList())
        } else {
            auditoriaDao.observarTodas().map { lista -> lista.map { it.aModelo() } }
        }
}
