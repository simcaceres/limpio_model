package com.example.limpio_moviles.model

object RegistroValidator {

    fun validar(uiState: RegistroUiState): Map<String, String> = buildMap {
        if (uiState.tipo.isBlank()) {
            put("tipo", "Selecciona un tipo")
        }

        val rango = Catalogos.rangoIntensidad(uiState.ambito)
        if (uiState.intensidad !in rango) {
            put(
                "intensidad",
                "La intensidad debe estar entre ${rango.first} y ${rango.last}"
            )
        }

        if (uiState.ambito == Ambito.ADICCIONES && uiState.contexto.isBlank()) {
            put("contexto", "Indica el desencadenante")
        }

        if (uiState.ambito == Ambito.DBT && uiState.estrategia.isBlank()) {
            put("estrategia", "Indica la habilidad practicada")
        }

        if (uiState.ambito == Ambito.NEURODESARROLLO && uiState.estrategia.isBlank()) {
            put("estrategia", "Indica la estrategia utilizada")
        }

        if (uiState.nota.length > 200) {
            put("nota", "La nota no puede superar 200 caracteres")
        }
    }
}
