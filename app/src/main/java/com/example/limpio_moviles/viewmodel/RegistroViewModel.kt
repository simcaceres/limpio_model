package com.example.limpio_moviles.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.limpio_moviles.data.local.RegistroEntity
import com.example.limpio_moviles.data.repository.PerfilRepository
import com.example.limpio_moviles.data.repository.RegistroRepository
import com.example.limpio_moviles.model.Ambito
import com.example.limpio_moviles.model.Catalogos
import com.example.limpio_moviles.model.PerfilUiState
import com.example.limpio_moviles.model.RegistroUiState
import com.example.limpio_moviles.model.RegistroValidator
import com.example.limpio_moviles.model.ResumenAmbito
import com.example.limpio_moviles.model.ResumenCalculator
import com.example.limpio_moviles.util.FotoStorage
import com.example.limpio_moviles.util.RecordatorioScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RegistroViewModel(
    application: Application,
    private val registroRepository: RegistroRepository,
    private val perfilRepository: PerfilRepository
) : AndroidViewModel(application) {

    var uiState by mutableStateOf(RegistroUiState())
        private set

    var perfil by mutableStateOf(PerfilUiState())
        private set

    var fotoPerfil by mutableStateOf<Bitmap?>(null)
        private set

    val registros: StateFlow<List<RegistroEntity>> = registroRepository.registros
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val resumen: StateFlow<List<ResumenAmbito>> = registroRepository.registros
        .map { listaRegistros -> ResumenCalculator.calcular(listaRegistros) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ResumenCalculator.calcular(emptyList())
        )

    init {
        val alias = perfilRepository.cargarAlias()
        val fotoPath = perfilRepository.cargarFotoPath()
        val recordatorioActivo = perfilRepository.cargarRecordatorio()
        perfil = PerfilUiState(
            alias = alias,
            fotoPath = fotoPath,
            recordatorioActivo = recordatorioActivo
        )
        viewModelScope.launch {
            val bitmap = withContext(Dispatchers.IO) { FotoStorage.cargar(fotoPath) }
            if (fotoPerfil == null) {
                fotoPerfil = bitmap
            }
        }
    }

    fun guardarAlias(alias: String) {
        val aliasLimpio = alias.trim()
        perfil = perfil.copy(alias = aliasLimpio)
        perfilRepository.guardarAlias(aliasLimpio)
    }

    fun actualizarFoto(bitmap: Bitmap) {
        fotoPerfil = bitmap
        viewModelScope.launch {
            val context = getApplication<Application>()
            val path = withContext(Dispatchers.IO) {
                runCatching { FotoStorage.guardar(context, bitmap) }.getOrNull()
            }
            if (path == null) {
                uiState = uiState.copy(mensaje = "No se pudo guardar la foto")
                return@launch
            }
            perfil = perfil.copy(fotoPath = path)
            perfilRepository.guardarFotoPath(path)
        }
    }

    fun activarRecordatorio(activo: Boolean) {
        perfil = perfil.copy(recordatorioActivo = activo)
        perfilRepository.guardarRecordatorio(activo)
        val context = getApplication<Application>()
        if (activo) {
            RecordatorioScheduler.programar(context)
        } else {
            RecordatorioScheduler.cancelar(context)
        }
    }

    fun prepararFormulario(ambito: Ambito, id: Int?) {
        if (id != null && id > 0) {
            uiState = uiState.copy(cargando = true, guardadoExitoso = false)
            viewModelScope.launch {
                val registro = runCatching { registroRepository.obtener(id) }.getOrNull()
                uiState = if (registro == null) {
                    RegistroUiState(ambito = ambito)
                } else {
                    RegistroUiState(
                        ambito = Ambito.desdeNombre(registro.ambito),
                        tipo = registro.tipo,
                        intensidad = registro.intensidad,
                        contexto = registro.contexto,
                        estrategia = registro.estrategia,
                        nota = registro.nota,
                        estadoObjetivo = registro.estadoObjetivo,
                        editandoId = registro.id
                    )
                }
            }
        } else {
            uiState = RegistroUiState(ambito = ambito)
        }
    }

    fun cambiarAmbito(ambito: Ambito) {
        uiState = uiState.copy(
            ambito = ambito,
            tipo = "",
            intensidad = Catalogos.rangoIntensidad(ambito).first,
            errores = emptyMap()
        )
    }

    fun actualizarTipo(tipo: String) {
        uiState = uiState.copy(tipo = tipo, errores = uiState.errores - "tipo")
    }

    fun actualizarIntensidad(intensidad: Int) {
        uiState = uiState.copy(
            intensidad = intensidad,
            errores = uiState.errores - "intensidad"
        )
    }

    fun actualizarContexto(contexto: String) {
        uiState = uiState.copy(contexto = contexto, errores = uiState.errores - "contexto")
    }

    fun actualizarEstrategia(estrategia: String) {
        uiState = uiState.copy(
            estrategia = estrategia,
            errores = uiState.errores - "estrategia"
        )
    }

    fun actualizarNota(nota: String) {
        uiState = uiState.copy(nota = nota, errores = uiState.errores - "nota")
    }

    fun actualizarEstadoObjetivo(estadoObjetivo: String) {
        uiState = uiState.copy(estadoObjetivo = estadoObjetivo)
    }

    fun guardar() {
        val errores = RegistroValidator.validar(uiState)
        if (errores.isNotEmpty()) {
            uiState = uiState.copy(errores = errores)
            return
        }

        val formulario = uiState
        uiState = uiState.copy(cargando = true, guardadoExitoso = false, errores = emptyMap())

        viewModelScope.launch {
            val registro = RegistroEntity(
                id = formulario.editandoId ?: 0,
                usuarioAlias = perfil.alias.ifBlank { "Usuario de prueba" },
                ambito = formulario.ambito.name,
                tipo = formulario.tipo.trim(),
                intensidad = formulario.intensidad,
                contexto = formulario.contexto.trim(),
                estrategia = formulario.estrategia.trim(),
                nota = formulario.nota.trim(),
                estadoObjetivo = formulario.estadoObjetivo.trim(),
                fechaHora = System.currentTimeMillis(),
                sincronizado = false
            )

            runCatching { registroRepository.guardar(registro) }
                .onSuccess {
                    uiState = uiState.copy(
                        cargando = false,
                        guardadoExitoso = true,
                        mensaje = if (formulario.editandoId != null) {
                            "Registro actualizado"
                        } else {
                            "Registro guardado"
                        }
                    )
                }
                .onFailure {
                    uiState = uiState.copy(
                        cargando = false,
                        guardadoExitoso = false,
                        mensaje = "No se pudo guardar el registro"
                    )
                }
        }
    }

    fun eliminar(registro: RegistroEntity) {
        viewModelScope.launch {
            runCatching { registroRepository.eliminar(registro) }
                .onSuccess {
                    uiState = uiState.copy(mensaje = "Registro eliminado")
                }
                .onFailure {
                    uiState = uiState.copy(mensaje = "No se pudo eliminar el registro")
                }
        }
    }

    fun sincronizar() {
        viewModelScope.launch {
            uiState = uiState.copy(cargando = true)
            runCatching { registroRepository.sincronizar() }
                .onSuccess { cantidad ->
                    uiState = uiState.copy(
                        cargando = false,
                        mensaje = if (cantidad > 0) {
                            "Sincronizados $cantidad registros"
                        } else {
                            "No hay registros pendientes"
                        }
                    )
                }
                .onFailure {
                    uiState = uiState.copy(
                        cargando = false,
                        mensaje = "No se pudo sincronizar"
                    )
                }
        }
    }

    fun cargarDemo() {
        viewModelScope.launch {
            uiState = uiState.copy(cargando = true)
            val alias = perfil.alias.ifBlank { "Usuario de prueba" }
            runCatching { registroRepository.cargarDemo(alias) }
                .onSuccess {
                    uiState = uiState.copy(
                        cargando = false,
                        mensaje = "Datos de demostración cargados"
                    )
                }
                .onFailure {
                    uiState = uiState.copy(
                        cargando = false,
                        mensaje = "No se pudieron cargar los datos de demostración"
                    )
                }
        }
    }

    fun borrarTodos() {
        viewModelScope.launch {
            runCatching { registroRepository.eliminarTodos() }
                .onSuccess {
                    uiState = uiState.copy(mensaje = "Registros eliminados")
                }
                .onFailure {
                    uiState = uiState.copy(mensaje = "No se pudieron eliminar los registros")
                }
        }
    }

    fun limpiarMensaje() {
        uiState = uiState.copy(mensaje = null)
    }

    fun limpiarFormulario() {
        uiState = RegistroUiState(ambito = uiState.ambito)
    }

    fun mostrarMensaje(mensaje: String) {
        uiState = uiState.copy(mensaje = mensaje)
    }
}

class RegistroViewModelFactory(
    private val application: Application,
    private val registroRepository: RegistroRepository,
    private val perfilRepository: PerfilRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegistroViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegistroViewModel(
                application,
                registroRepository,
                perfilRepository
            ) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
