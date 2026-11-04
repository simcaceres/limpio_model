package com.example.limpio_moviles.model

object Catalogos {

    fun tipos(ambito: Ambito): List<String> = when (ambito) {
        Ambito.NEURODESARROLLO -> listOf("Rutina", "Tarea", "Sobrecarga", "Autorregulación")
        Ambito.DBT -> listOf(
            "Mindfulness",
            "Tolerancia al malestar",
            "Regulación emocional",
            "Efectividad interpersonal"
        )
        Ambito.ANIMO -> listOf("Feliz", "Tranquilo", "Neutro", "Ansioso", "Triste", "Irritable")
        Ambito.ADICCIONES -> listOf("Craving", "Impulso", "Recaída", "Objetivo cumplido")
    }

    fun rangoIntensidad(ambito: Ambito): IntRange =
        if (ambito == Ambito.ADICCIONES) 0..10 else 1..5

    fun etiquetaContexto(ambito: Ambito): String = when (ambito) {
        Ambito.NEURODESARROLLO -> "Contexto o situación"
        Ambito.DBT -> "Emoción o situación"
        Ambito.ANIMO -> "Contexto"
        Ambito.ADICCIONES -> "Desencadenante"
    }

    fun etiquetaEstrategia(ambito: Ambito): String = when (ambito) {
        Ambito.NEURODESARROLLO -> "Estrategia utilizada"
        Ambito.DBT -> "Habilidad practicada"
        Ambito.ANIMO -> "Acción de autocuidado"
        Ambito.ADICCIONES -> "Acción protectora"
    }

    @Suppress("UNUSED_PARAMETER")
    fun etiquetaNota(ambito: Ambito): String = "Nota opcional (máx. 200 caracteres)"
}
