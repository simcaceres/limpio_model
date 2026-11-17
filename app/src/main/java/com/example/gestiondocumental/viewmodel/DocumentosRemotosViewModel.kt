package com.example.gestiondocumental.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestiondocumental.data.remote.ApiConfig
import com.example.gestiondocumental.data.repository.DocumentosRemotosRepository
import com.example.gestiondocumental.model.DocumentosRemotosUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/**
 * ViewModel de la pantalla de documentos remotos.
 *
 * Consulta la API REST simulada, expone el estado como [StateFlow] y traduce
 * los fallos tecnicos a mensajes legibles para el usuario.
 */
class DocumentosRemotosViewModel(
    private val repo: DocumentosRemotosRepository
) : ViewModel() {

    private val _estado = MutableStateFlow<DocumentosRemotosUiState>(
        DocumentosRemotosUiState.Cargando
    )
    val estado: StateFlow<DocumentosRemotosUiState> = _estado.asStateFlow()

    private var trabajo: Job? = null

    init {
        cargar()
    }

    /** Vuelve a consultar la API, cancelando cualquier carga anterior. */
    fun cargar() {
        trabajo?.cancel()
        trabajo = viewModelScope.launch {
            _estado.value = DocumentosRemotosUiState.Cargando
            repo.obtenerDocumentos().fold(
                onSuccess = { documentos ->
                    _estado.value = DocumentosRemotosUiState.Datos(documentos)
                },
                onFailure = { error ->
                    _estado.value = DocumentosRemotosUiState.Error(mensajeLegible(error))
                }
            )
        }
    }

    private fun mensajeLegible(error: Throwable): String = when (error) {
        is IOException ->
            "No se pudo conectar con la API (${ApiConfig.BASE_URL}). " +
                "Verifica que la API simulada este en ejecucion."
        is HttpException -> "El servidor respondio con error ${error.code()}."
        else -> "Ocurrió un error inesperado al cargar los documentos."
    }
}
