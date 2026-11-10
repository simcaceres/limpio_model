package com.example.gestiondocumental.data.local

import com.example.gestiondocumental.model.CategoriaDocumento
import com.example.gestiondocumental.model.Documento
import com.example.gestiondocumental.model.EstadoDocumento
import com.example.gestiondocumental.model.EstadoSolicitud
import com.example.gestiondocumental.model.RegistroAuditoria
import com.example.gestiondocumental.model.Rol
import com.example.gestiondocumental.model.SolicitudModificacion
import com.example.gestiondocumental.model.TipoCambio
import com.example.gestiondocumental.model.Usuario
import com.example.gestiondocumental.model.VersionDocumento

fun UsuarioEntity.aModelo(): Usuario = Usuario(
    id = id,
    rut = rut,
    nombre = nombre,
    correo = correo,
    rol = Rol.valueOf(rol),
    dependencia = dependencia,
    tipoContrato = tipoContrato,
    activo = activo
)

fun DocumentoEntity.aModelo(): Documento = Documento(
    id = id,
    usuarioId = usuarioId,
    propietario = propietario,
    categoria = CategoriaDocumento.valueOf(categoria),
    titulo = titulo,
    estado = EstadoDocumento.valueOf(estado),
    versionActual = versionActual,
    fechaVencimiento = fechaVencimiento,
    dependencia = dependencia,
    archivoNombre = archivoNombre
)

fun VersionEntity.aModelo(): VersionDocumento = VersionDocumento(
    id = id,
    documentoId = documentoId,
    numeroVersion = numeroVersion,
    autor = autor,
    fecha = fecha,
    comentario = comentario
)

fun SolicitudEntity.aModelo(): SolicitudModificacion = SolicitudModificacion(
    id = id,
    documentoId = documentoId,
    documentoTitulo = documentoTitulo,
    solicitante = solicitante,
    tipoCambio = TipoCambio.valueOf(tipoCambio),
    datosPropuestos = datosPropuestos,
    estado = EstadoSolicitud.valueOf(estado),
    revisor = revisor,
    motivoRechazo = motivoRechazo,
    fecha = fecha,
    observacion = observacion
)

fun AuditoriaEntity.aModelo(): RegistroAuditoria = RegistroAuditoria(
    id = id,
    usuario = usuario,
    fechaHora = fechaHora,
    entidad = entidad,
    accion = accion,
    detalle = detalle,
    resultado = resultado
)
