package com.example.limpio_moviles.model

data class RegistroUiState(
    val ambito: Ambito = Ambito.NEURODESARROLLO,
    val tipo: String = "",
    val intensidad: Int = 3,
    val contexto: String = "",
    val estrategia: String = "",
    val nota: String = "",
    val estadoObjetivo: String = "",
    val editandoId: Int? = null,
    val errores: Map<String, String> = emptyMap(),
    val guardadoExitoso: Boolean = false,
    val cargando: Boolean = false,
    val mensaje: String? = null
)
