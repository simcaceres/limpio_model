package com.example.limpio_moviles.model

import com.example.limpio_moviles.data.local.RegistroEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ResumenCalculatorTest {

    private fun registro(
        ambito: Ambito,
        intensidad: Int,
        fechaHora: Long,
        id: Int = 0
    ) = RegistroEntity(
        id = id,
        usuarioAlias = "tester",
        ambito = ambito.name,
        tipo = "Tipo de prueba",
        intensidad = intensidad,
        fechaHora = fechaHora
    )

    private fun List<ResumenAmbito>.porAmbito(ambito: Ambito): ResumenAmbito =
        first { it.ambito == ambito }

    @Test
    fun listaVaciaDevuelveUnResumenPorAmbitoConCantidadCeroPromedioCeroYUltimaFechaNula() {
        // Act
        val resumenes = ResumenCalculator.calcular(emptyList())

        // Assert
        assertEquals(Ambito.entries.size, resumenes.size)
        assertEquals(4, resumenes.size)
        Ambito.entries.forEach { ambito ->
            val resumen = resumenes.porAmbito(ambito)
            assertEquals("cantidad para $ambito", 0, resumen.cantidad)
            assertEquals("promedio para $ambito", 0.0, resumen.promedioIntensidad, 0.0001)
            assertNull("ultimaFecha para $ambito", resumen.ultimaFecha)
        }
    }

    @Test
    fun conRegistrosRepartidosCalculaCantidadPromedioYUltimaFechaPorAmbito() {
        // Arrange
        val registros = listOf(
            registro(Ambito.NEURODESARROLLO, intensidad = 2, fechaHora = 100L, id = 1),
            registro(Ambito.NEURODESARROLLO, intensidad = 4, fechaHora = 300L, id = 2),
            registro(Ambito.ANIMO, intensidad = 5, fechaHora = 200L, id = 3),
            registro(Ambito.ADICCIONES, intensidad = 10, fechaHora = 500L, id = 4)
        )

        // Act
        val resumenes = ResumenCalculator.calcular(registros)

        // Assert
        val neuro = resumenes.porAmbito(Ambito.NEURODESARROLLO)
        assertEquals(2, neuro.cantidad)
        assertEquals(3.0, neuro.promedioIntensidad, 0.0001)
        assertEquals(300L, neuro.ultimaFecha)

        val animo = resumenes.porAmbito(Ambito.ANIMO)
        assertEquals(1, animo.cantidad)
        assertEquals(5.0, animo.promedioIntensidad, 0.0001)
        assertEquals(200L, animo.ultimaFecha)

        val adicciones = resumenes.porAmbito(Ambito.ADICCIONES)
        assertEquals(1, adicciones.cantidad)
        assertEquals(10.0, adicciones.promedioIntensidad, 0.0001)
        assertEquals(500L, adicciones.ultimaFecha)
    }

    @Test
    fun unAmbitoSinRegistrosQuedaEnCeroAunqueOtrosTenganDatos() {
        // Arrange: DBT no tiene registros
        val registros = listOf(
            registro(Ambito.NEURODESARROLLO, intensidad = 4, fechaHora = 10L, id = 1),
            registro(Ambito.ANIMO, intensidad = 2, fechaHora = 20L, id = 2),
            registro(Ambito.ADICCIONES, intensidad = 7, fechaHora = 30L, id = 3)
        )

        // Act
        val resumenes = ResumenCalculator.calcular(registros)

        // Assert
        val dbt = resumenes.porAmbito(Ambito.DBT)
        assertEquals(0, dbt.cantidad)
        assertEquals(0.0, dbt.promedioIntensidad, 0.0001)
        assertNull(dbt.ultimaFecha)

        // Los demás ámbitos sí tienen datos
        assertTrue(resumenes.porAmbito(Ambito.NEURODESARROLLO).cantidad > 0)
        assertTrue(resumenes.porAmbito(Ambito.ANIMO).cantidad > 0)
        assertTrue(resumenes.porAmbito(Ambito.ADICCIONES).cantidad > 0)
    }

    @Test
    fun ultimaFechaEsElMaximoDeLasFechasDelAmbito() {
        // Arrange
        val registros = listOf(
            registro(Ambito.ANIMO, intensidad = 1, fechaHora = 50L, id = 1),
            registro(Ambito.ANIMO, intensidad = 3, fechaHora = 900L, id = 2),
            registro(Ambito.ANIMO, intensidad = 2, fechaHora = 400L, id = 3)
        )

        // Act
        val resumenes = ResumenCalculator.calcular(registros)

        // Assert
        val animo = resumenes.porAmbito(Ambito.ANIMO)
        assertEquals(3, animo.cantidad)
        assertEquals(2.0, animo.promedioIntensidad, 0.0001)
        assertEquals(900L, animo.ultimaFecha)
    }
}
