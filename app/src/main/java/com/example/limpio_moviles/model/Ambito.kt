package com.example.limpio_moviles.model

enum class Ambito(val etiqueta: String, val descripcion: String) {
    NEURODESARROLLO("Neurodesarrollo", "Rutinas, tareas y autorregulación"),
    DBT("Habilidades DBT", "Mindfulness, tolerancia, regulación e interpersonal"),
    ANIMO("Estado de ánimo", "Check-in de emoción e intensidad"),
    ADICCIONES("Adicciones", "Impulsos, desencadenantes y acciones protectoras");

    companion object {
        fun desdeNombre(nombre: String?): Ambito =
            entries.firstOrNull { it.name == nombre } ?: NEURODESARROLLO
    }
}
