package com.example.limpio_moviles.model

import com.example.limpio_moviles.data.local.RegistroEntity

object ResumenCalculator {

    fun calcular(registros: List<RegistroEntity>): List<ResumenAmbito> =
        Ambito.entries.map { ambito ->
            val registrosDelAmbito = registros.filter { it.ambito == ambito.name }
            ResumenAmbito(
                ambito = ambito,
                cantidad = registrosDelAmbito.size,
                promedioIntensidad = if (registrosDelAmbito.isEmpty()) {
                    0.0
                } else {
                    registrosDelAmbito.sumOf { it.intensidad }.toDouble() /
                        registrosDelAmbito.size
                },
                ultimaFecha = registrosDelAmbito.maxOfOrNull { it.fechaHora }
            )
        }
}
