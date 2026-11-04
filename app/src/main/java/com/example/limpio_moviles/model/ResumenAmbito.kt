package com.example.limpio_moviles.model

data class ResumenAmbito(
    val ambito: Ambito,
    val cantidad: Int,
    val promedioIntensidad: Double,
    val ultimaFecha: Long?
)
