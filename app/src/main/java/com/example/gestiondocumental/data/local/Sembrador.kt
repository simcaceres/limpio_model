package com.example.gestiondocumental.data.local

import com.example.gestiondocumental.model.CategoriaDocumento
import com.example.gestiondocumental.model.EstadoDocumento
import com.example.gestiondocumental.model.EstadoSolicitud
import com.example.gestiondocumental.model.Rol
import com.example.gestiondocumental.model.TipoCambio
import com.example.gestiondocumental.util.ahora

/**
 * Datos de demostración para que la app sea navegable sin backend.
 * Se cargan sólo la primera vez (si la tabla de usuarios está vacía).
 */
object Sembrador {

    suspend fun sembrarSiVacio(db: GestionDatabase) {
        if (db.usuarioDao().contar() > 0) return

        db.dependenciaDao().insertar(DependenciaEntity(1, "Dirección de Salud"))
        db.dependenciaDao().insertar(DependenciaEntity(2, "Recursos Humanos"))
        db.dependenciaDao().insertar(DependenciaEntity(3, "Atención Primaria"))
        db.dependenciaDao().insertar(DependenciaEntity(4, "Hospital Base"))

        val usuarios = listOf(
            UsuarioEntity(1, "11111111-1", "Ana Pérez", "ana.perez@salud.cl", Rol.FUNCIONARIO.name, "Atención Primaria", "INDEFINIDO", true, "1234"),
            UsuarioEntity(2, "22222222-2", "Bruno Díaz", "bruno.diaz@salud.cl", Rol.JEFATURA.name, "Atención Primaria", "INDEFINIDO", true, "1234"),
            UsuarioEntity(3, "33333333-3", "Carla Soto", "carla.soto@salud.cl", Rol.ADMINISTRATIVO.name, "Recursos Humanos", "PLAZO_FIJO", true, "1234"),
            UsuarioEntity(4, "44444444-4", "Diego Rojas", "diego.rojas@salud.cl", Rol.RRHH.name, "Recursos Humanos", "INDEFINIDO", true, "1234"),
            UsuarioEntity(5, "55555555-5", "Elena Muñoz", "elena.munoz@salud.cl", Rol.ADMIN_SISTEMA.name, "Dirección de Salud", "INDEFINIDO", true, "1234"),
            UsuarioEntity(6, "66666666-6", "Felipe Vega", "felipe.vega@salud.cl", Rol.TI_CIBERSEGURIDAD.name, "Dirección de Salud", "INDEFINIDO", true, "1234"),
            UsuarioEntity(7, "77777777-7", "Gabriela Paz", "gabriela.paz@salud.cl", Rol.SUPERVISOR.name, "Hospital Base", "INDEFINIDO", true, "1234")
        )
        usuarios.forEach { db.usuarioDao().insertar(it) }

        val documentos = listOf(
            DocumentoEntity(
                usuarioId = 1, propietario = "Ana Pérez",
                categoria = CategoriaDocumento.CONTRATO.name, titulo = "Contrato de trabajo 2026",
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
                categoria = CategoriaDocumento.CERTIFICADO.name, titulo = "Certificado de antigüedad",
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
        documentos.forEach { db.documentoDao().insertar(it) }

        for (id in 1L..6L) {
            db.versionDao().insertar(
                VersionEntity(
                    documentoId = id,
                    numeroVersion = 1,
                    autor = "Sistema",
                    fecha = ahora(),
                    comentario = "Versión inicial del documento"
                )
            )
        }

        // El documento 3 nace con versionActual = 2: se agrega su segunda versión.
        db.versionDao().insertar(
            VersionEntity(
                documentoId = 3,
                numeroVersion = 2,
                autor = "Sistema",
                fecha = ahora(),
                comentario = "Actualización de certificado"
            )
        )

        db.solicitudDao().insertar(
            SolicitudEntity(
                documentoId = 1,
                documentoTitulo = "Contrato de trabajo 2026",
                solicitante = "Ana Pérez",
                tipoCambio = TipoCambio.CON_REVISION.name,
                datosPropuestos = "Actualizar dirección a Av. Siempre Viva 742",
                estado = EstadoSolicitud.PENDIENTE.name,
                revisor = null,
                motivoRechazo = null,
                fecha = ahora()
            )
        )

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
