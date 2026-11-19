package com.example.gestiondocumental.data.local

import androidx.room.withTransaction
import com.example.gestiondocumental.model.CategoriaDocumento
import com.example.gestiondocumental.model.EstadoDocumento
import com.example.gestiondocumental.model.EstadoSolicitud
import com.example.gestiondocumental.model.Rol
import com.example.gestiondocumental.model.TipoCambio
import com.example.gestiondocumental.util.ahora

/**
 * Datos de demostración para que la app sea navegable sin backend.
 *
 * La siembra es transaccional y convergente: si falla a la mitad, Room revierte
 * todos los cambios y el siguiente arranque vuelve a intentarlo. Cada fila se
 * inserta sólo si no existe (por id o por clave natural), de modo que también
 * repara siembras parciales heredadas sin duplicar datos ni invalidar Flows
 * cuando ya está todo completo.
 */
object Sembrador {

    private const val TITULO_CERTIFICADO = "Certificado de antigüedad"
    private const val TITULO_CONTRATO = "Contrato de trabajo 2026"
    private const val SOLICITANTE_DEMO = "Ana Pérez"

    suspend fun sembrarSiVacio(db: GestionDatabase) {
        db.withTransaction {
            // La auditoría SEED representa la carga inicial: sólo debe crearse
            // si la tabla de documentos estaba vacía antes de esta pasada.
            val habiaDocumentos = db.documentoDao().contar() > 0

            sembrarDependencias(db)
            sembrarUsuarios(db)
            val idPorTitulo = sembrarDocumentos(db)
            sembrarVersiones(db, idPorTitulo)
            sembrarSolicitud(db, idPorTitulo)
            if (!habiaDocumentos) sembrarAuditoriaSeed(db)
        }
    }

    /** Dependencias base: se insertan sólo si su id no existe. */
    private suspend fun sembrarDependencias(db: GestionDatabase) {
        val dependencias = listOf(
            DependenciaEntity(1, "Dirección de Salud"),
            DependenciaEntity(2, "Recursos Humanos"),
            DependenciaEntity(3, "Atención Primaria"),
            DependenciaEntity(4, "Hospital Base")
        )
        dependencias.forEach { dependencia ->
            if (db.dependenciaDao().contarPorId(dependencia.id) == 0) {
                db.dependenciaDao().insertar(dependencia)
            }
        }
    }

    /** Usuarios demo: completa los ids faltantes sin duplicar los existentes. */
    private suspend fun sembrarUsuarios(db: GestionDatabase) {
        val usuarios = listOf(
            UsuarioEntity(1, "11111111-1", "Ana Pérez", "ana.perez@salud.cl", Rol.FUNCIONARIO.name, "Atención Primaria", "INDEFINIDO", true, "1234"),
            UsuarioEntity(2, "22222222-2", "Bruno Díaz", "bruno.diaz@salud.cl", Rol.JEFATURA.name, "Atención Primaria", "INDEFINIDO", true, "1234"),
            UsuarioEntity(3, "33333333-3", "Carla Soto", "carla.soto@salud.cl", Rol.ADMINISTRATIVO.name, "Recursos Humanos", "PLAZO_FIJO", true, "1234"),
            UsuarioEntity(4, "44444444-4", "Diego Rojas", "diego.rojas@salud.cl", Rol.RRHH.name, "Recursos Humanos", "INDEFINIDO", true, "1234"),
            UsuarioEntity(5, "55555555-5", "Elena Muñoz", "elena.munoz@salud.cl", Rol.ADMIN_SISTEMA.name, "Dirección de Salud", "INDEFINIDO", true, "1234"),
            UsuarioEntity(6, "66666666-6", "Felipe Vega", "felipe.vega@salud.cl", Rol.TI_CIBERSEGURIDAD.name, "Dirección de Salud", "INDEFINIDO", true, "1234"),
            UsuarioEntity(7, "77777777-7", "Gabriela Paz", "gabriela.paz@salud.cl", Rol.SUPERVISOR.name, "Hospital Base", "INDEFINIDO", true, "1234")
        )
        usuarios.forEach { usuario ->
            if (db.usuarioDao().porId(usuario.id) == null) {
                db.usuarioDao().insertar(usuario)
            }
        }
    }

    /**
     * Documentos demo: se buscan por título (clave natural estable) y se usa el
     * id real del documento, ya sea existente o recién insertado.
     */
    private suspend fun sembrarDocumentos(db: GestionDatabase): Map<String, Long> {
        val documentos = listOf(
            DocumentoEntity(
                usuarioId = 1, propietario = "Ana Pérez",
                categoria = CategoriaDocumento.CONTRATO.name, titulo = TITULO_CONTRATO,
                estado = EstadoDocumento.VIGENTE.name, versionActual = 1,
                fechaVencimiento = "2026-12-31", dependencia = "Atención Primaria"
            ),
            DocumentoEntity(
                usuarioId = 1, propietario = "Ana Pérez",
                categoria = CategoriaDocumento.LICENCIA_MEDICA.name, titulo = "Licencia médica marzo",
                estado = EstadoDocumento.VIGENTE.name, versionActual = 1,
                fechaVencimiento = "2026-03-20", dependencia = "Atención Primaria"
            ),
            DocumentoEntity(
                usuarioId = 2, propietario = "Bruno Díaz",
                categoria = CategoriaDocumento.CERTIFICADO.name, titulo = TITULO_CERTIFICADO,
                estado = EstadoDocumento.APROBADO.name, versionActual = 2,
                fechaVencimiento = null, dependencia = "Atención Primaria"
            ),
            DocumentoEntity(
                usuarioId = 3, propietario = "Carla Soto",
                categoria = CategoriaDocumento.PERMISO.name, titulo = "Solicitud de permiso administrativo",
                estado = EstadoDocumento.PENDIENTE_REVISION.name, versionActual = 1,
                fechaVencimiento = null, dependencia = "Recursos Humanos"
            ),
            DocumentoEntity(
                usuarioId = 1, propietario = "Ana Pérez",
                categoria = CategoriaDocumento.ANTECEDENTE_SENSIBLE.name, titulo = "Antecedente personal reservado",
                estado = EstadoDocumento.VIGENTE.name, versionActual = 1,
                fechaVencimiento = null, dependencia = "Atención Primaria"
            ),
            DocumentoEntity(
                usuarioId = 7, propietario = "Gabriela Paz",
                categoria = CategoriaDocumento.REPORTE.name, titulo = "Reporte de cumplimiento documental Q1",
                estado = EstadoDocumento.VIGENTE.name, versionActual = 1,
                fechaVencimiento = null, dependencia = "Hospital Base"
            )
        )

        return documentos.associate { documento ->
            val existente = db.documentoDao().porTitulo(documento.titulo)
            val id = existente?.id ?: db.documentoDao().insertar(documento)
            documento.titulo to id
        }
    }

    /** Versiones demo: se insertan sólo si ese número de versión no existe. */
    private suspend fun sembrarVersiones(db: GestionDatabase, idPorTitulo: Map<String, Long>) {
        val versiones = listOf(
            Triple(TITULO_CONTRATO, 1, "Versión inicial del documento"),
            Triple("Licencia médica marzo", 1, "Versión inicial del documento"),
            Triple(TITULO_CERTIFICADO, 1, "Versión inicial del documento"),
            Triple("Solicitud de permiso administrativo", 1, "Versión inicial del documento"),
            Triple("Antecedente personal reservado", 1, "Versión inicial del documento"),
            Triple("Reporte de cumplimiento documental Q1", 1, "Versión inicial del documento"),
            // El certificado nace con versionActual = 2: se agrega su segunda versión.
            Triple(TITULO_CERTIFICADO, 2, "Actualización de certificado")
        )

        versiones.forEach { (titulo, numeroVersion, comentario) ->
            val documentoId = idPorTitulo.getValue(titulo)
            val yaExiste = db.versionDao()
                .listarPorDocumento(documentoId)
                .any { it.numeroVersion == numeroVersion }
            if (!yaExiste) {
                db.versionDao().insertar(
                    VersionEntity(
                        documentoId = documentoId,
                        numeroVersion = numeroVersion,
                        autor = "Sistema",
                        fecha = ahora(),
                        comentario = comentario
                    )
                )
            }
        }
    }

    /** Solicitud demo: se inserta sólo si no existe la misma combinación título/solicitante. */
    private suspend fun sembrarSolicitud(db: GestionDatabase, idPorTitulo: Map<String, Long>) {
        val yaExiste = db.solicitudDao()
            .contarPorTituloYSolicitante(TITULO_CONTRATO, SOLICITANTE_DEMO) > 0
        if (yaExiste) return

        db.solicitudDao().insertar(
            SolicitudEntity(
                documentoId = idPorTitulo.getValue(TITULO_CONTRATO),
                documentoTitulo = TITULO_CONTRATO,
                solicitante = SOLICITANTE_DEMO,
                tipoCambio = TipoCambio.CON_REVISION.name,
                datosPropuestos = "Actualizar dirección a Av. Siempre Viva 742",
                estado = EstadoSolicitud.PENDIENTE.name,
                revisor = null,
                motivoRechazo = null,
                fecha = ahora()
            )
        )
    }

    private suspend fun sembrarAuditoriaSeed(db: GestionDatabase) {
        db.auditoriaDao().insertar(
            AuditoriaEntity(
                usuario = "Sistema",
                fechaHora = ahora(),
                entidad = "Sistema",
                accion = "SEED",
                detalle = "Datos de demostración cargados",
                resultado = "OK"
            )
        )
    }
}
