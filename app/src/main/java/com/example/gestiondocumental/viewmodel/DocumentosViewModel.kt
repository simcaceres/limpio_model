package com.example.gestiondocumental.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestiondocumental.data.repository.DocumentoRepository
import com.example.gestiondocumental.data.repository.SesionRepository
import com.example.gestiondocumental.data.repository.UsuarioRepository
import com.example.gestiondocumental.model.CategoriaDocumento
import com.example.gestiondocumental.model.Documento
import com.example.gestiondocumental.model.SolicitudModificacion
import com.example.gestiondocumental.model.Usuario
import com.example.gestiondocumental.model.VersionDocumento
import com.example.gestiondocumental.util.NotificacionHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

/**
 * ViewModel único para el flujo de documentos y aprobaciones (RF12).
 * Mantiene el estado de la lista, el documento seleccionado, sus versiones
 * y las solicitudes pendientes de revisión.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DocumentosViewModel(
    private val sesion: SesionRepository,
    private val repo: DocumentoRepository,
    private val usuarioRepo: UsuarioRepository? = null,
    private val appContext: Context? = null
) : ViewModel() {

    val usuario: StateFlow<Usuario?> = sesion.actual

    private val _usuarios = MutableStateFlow<List<Usuario>>(emptyList())
    val usuarios: StateFlow<List<Usuario>> = _usuarios.asStateFlow()

    private val _documentos = MutableStateFlow<List<Documento>>(emptyList())
    val documentos: StateFlow<List<Documento>> = _documentos.asStateFlow()

    private val _solicitudes = MutableStateFlow<List<SolicitudModificacion>>(emptyList())
    val solicitudes: StateFlow<List<SolicitudModificacion>> = _solicitudes.asStateFlow()

    private val _pendientes = MutableStateFlow<List<SolicitudModificacion>>(emptyList())
    val pendientes: StateFlow<List<SolicitudModificacion>> = _pendientes.asStateFlow()

    private val _documentosReportes = MutableStateFlow<List<Documento>>(emptyList())
    val documentosReportes: StateFlow<List<Documento>> = _documentosReportes.asStateFlow()

    private val _seleccionado = MutableStateFlow<Documento?>(null)
    val seleccionado: StateFlow<Documento?> = _seleccionado.asStateFlow()

    private val _versiones = MutableStateFlow<List<VersionDocumento>>(emptyList())
    val versiones: StateFlow<List<VersionDocumento>> = _versiones.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    init {
        viewModelScope.launch {
            sesion.actual.filterNotNull().flatMapLatest { u -> repo.observarDocumentosPara(u) }
                .collect { _documentos.value = it }
        }
        viewModelScope.launch {
            sesion.actual.filterNotNull().flatMapLatest { u -> repo.observarSolicitudesPara(u) }
                .collect { _solicitudes.value = it }
        }
        viewModelScope.launch {
            sesion.actual.filterNotNull().flatMapLatest { u -> repo.observarPendientesPara(u) }
                .collect { _pendientes.value = it }
        }
        viewModelScope.launch {
            sesion.actual.filterNotNull().flatMapLatest { u -> repo.observarParaReportes(u) }
                .collect { _documentosReportes.value = it }
        }
        usuarioRepo?.let { repositorioUsuarios ->
            viewModelScope.launch {
                sesion.actual.filterNotNull().flatMapLatest { u -> repositorioUsuarios.observarPara(u) }
                    .collect { _usuarios.value = it }
            }
        }
    }

    fun seleccionar(documento: Documento) {
        _seleccionado.value = documento
        viewModelScope.launch {
            _versiones.value = repo.versionesDe(documento.id)
        }
    }

    fun refrescarSeleccionado() {
        val actual = _seleccionado.value ?: return
        viewModelScope.launch {
            val fresco = repo.porId(actual.id)
            if (fresco != null) {
                _seleccionado.value = fresco
                _versiones.value = repo.versionesDe(fresco.id)
            }
        }
    }

    fun cargarDocumento(
        propietario: Usuario,
        categoria: CategoriaDocumento,
        titulo: String,
        fechaVencimiento: String?,
        archivoNombre: String?
    ) {
        val actor = usuario.value ?: return
        viewModelScope.launch {
            _cargando.value = true
            repo.cargarDocumento(
                propietario = propietario,
                categoria = categoria,
                titulo = titulo,
                fechaVencimiento = fechaVencimiento,
                archivoNombre = archivoNombre,
                actor = actor
            )
                .onSuccess { _mensaje.value = "Documento cargado correctamente." }
                .onFailure { _mensaje.value = it.message ?: "No se pudo cargar el documento." }
            _cargando.value = false
        }
    }

    fun solicitarModificacion(datos: String) {
        val u = usuario.value ?: return
        val d = _seleccionado.value ?: return
        if (datos.isBlank()) {
            _mensaje.value = "Describe el cambio que quieres solicitar."
            return
        }
        viewModelScope.launch {
            _cargando.value = true
            repo.solicitarModificacion(d, u, datos)
                .onSuccess {
                    _mensaje.value = "Solicitud registrada correctamente."
                    refrescarSeleccionado()
                }
                .onFailure { _mensaje.value = it.message ?: "No se pudo registrar la solicitud." }
            _cargando.value = false
        }
    }

    fun aprobar(solicitud: SolicitudModificacion) {
        val u = usuario.value ?: return
        viewModelScope.launch {
            _cargando.value = true
            repo.aprobar(solicitud.id, u)
                .onSuccess {
                    _mensaje.value = "Solicitud #${solicitud.id} aprobada."
                    refrescarSeleccionado()
                    appContext?.let {
                        NotificacionHelper.notificar(
                            it,
                            solicitud.id.toInt(),
                            "Solicitud aprobada",
                            "La solicitud #${solicitud.id} fue aprobada."
                        )
                    }
                }
                .onFailure { _mensaje.value = it.message ?: "No se pudo aprobar la solicitud." }
            _cargando.value = false
        }
    }

    fun rechazar(solicitud: SolicitudModificacion, motivo: String) {
        val u = usuario.value ?: return
        viewModelScope.launch {
            _cargando.value = true
            repo.rechazar(solicitud.id, u, motivo)
                .onSuccess {
                    _mensaje.value = "Solicitud #${solicitud.id} rechazada."
                    appContext?.let {
                        NotificacionHelper.notificar(
                            it,
                            solicitud.id.toInt(),
                            "Solicitud rechazada",
                            "La solicitud #${solicitud.id} fue rechazada."
                        )
                    }
                }
                .onFailure { _mensaje.value = it.message ?: "No se pudo rechazar la solicitud." }
            _cargando.value = false
        }
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }
}
