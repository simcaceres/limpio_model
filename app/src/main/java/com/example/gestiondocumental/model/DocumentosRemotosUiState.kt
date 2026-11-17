package com.example.gestiondocumental.model

/** Estados de la pantalla de documentos remotos (API REST simulada). */
sealed interface DocumentosRemotosUiState {

    /** La consulta esta en curso. */
    data object Cargando : DocumentosRemotosUiState

    /** La consulta termino y entrego documentos. */
    data class Datos(val documentos: List<Documento>) : DocumentosRemotosUiState

    /** La consulta fallo; [mensaje] es apto para mostrar al usuario. */
    data class Error(val mensaje: String) : DocumentosRemotosUiState
}
