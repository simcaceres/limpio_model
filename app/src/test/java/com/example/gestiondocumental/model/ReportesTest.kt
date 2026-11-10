package com.example.gestiondocumental.model

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReportesTest {

    private val hoy = LocalDate.of(2026, 11, 8)

    private fun documento(
        id: Long,
        titulo: String,
        fecha: String?,
        categoria: CategoriaDocumento = CategoriaDocumento.CONTRATO,
        estado: EstadoDocumento = EstadoDocumento.VIGENTE
    ): Documento = Documento(
        id = id,
        usuarioId = 1,
        propietario = "Ana Pérez",
        categoria = categoria,
        titulo = titulo,
        estado = estado,
        versionActual = 1,
        fechaVencimiento = fecha,
        dependencia = "Atención Primaria"
    )

    private val documentos = listOf(
        documento(1, "Vencido", "2026-11-01"),
        documento(2, "Por vencer 30", "2026-11-20"),
        documento(3, "Por vencer 60", "2026-12-20"),
        documento(
            4, "Sin fecha", null,
            categoria = CategoriaDocumento.PERMISO,
            estado = EstadoDocumento.PENDIENTE_REVISION
        )
    )

    @Test
    fun resumenVencimientosCuentaCadaBucket() {
        val resumen = Reportes.resumenVencimientos(documentos, hoy)

        assertEquals(1, resumen.vencidos)
        assertEquals(1, resumen.porVencer30)
        assertEquals(1, resumen.porVencer60)
        assertEquals(1, resumen.sinVencimiento)
        assertEquals(4, resumen.total)
    }

    @Test
    fun documentosPorVencerOrdenaAscendenteYExcluyeVencidosYSinFecha() {
        val porVencer = Reportes.documentosPorVencer(documentos, hoy)

        assertEquals(listOf("Por vencer 30", "Por vencer 60"), porVencer.map { it.titulo })
    }

    @Test
    fun documentosPorVencerRespetaElLimiteDeDias() {
        val porVencer = Reportes.documentosPorVencer(documentos, hoy, dias = 30)

        assertEquals(listOf("Por vencer 30"), porVencer.map { it.titulo })
    }

    @Test
    fun diasHastaVencimientoCalculaYDescartaFechasInvalidas() {
        assertEquals(12L, Reportes.diasHastaVencimiento("2026-11-20", hoy))
        assertEquals(-7L, Reportes.diasHastaVencimiento("2026-11-01", hoy))

        assertNull(Reportes.diasHastaVencimiento(null, hoy))
        assertNull(Reportes.diasHastaVencimiento("   ", hoy))
        assertNull(Reportes.diasHastaVencimiento("2026-13-01", hoy))
        assertNull(Reportes.diasHastaVencimiento("08/11/2026", hoy))
    }

    @Test
    fun diasHastaVencimientoDevuelveNullParaFechasNulasEnBlancoOMalFormadas() {
        assertNull(Reportes.diasHastaVencimiento(null, hoy))
        assertNull(Reportes.diasHastaVencimiento("", hoy))
        assertNull(Reportes.diasHastaVencimiento("   ", hoy))
        assertNull(Reportes.diasHastaVencimiento("2026-13-40", hoy))
    }

    @Test
    fun documentosPorVencerIncluyeHoyYElLimiteDeSesentaDias() {
        val porVencer = listOf(
            documento(1, "Vencido", "2026-11-07"),
            documento(2, "Hoy", "2026-11-08"),
            documento(3, "Día 60", "2027-01-07"),
            documento(4, "Sin fecha", null),
            documento(5, "Fuera del límite", "2027-01-08")
        )

        val resultado = Reportes.documentosPorVencer(porVencer, hoy)

        assertEquals(listOf("Hoy", "Día 60"), resultado.map { it.titulo })
    }

    @Test
    fun documentosPorVencerOrdenaDeFormaAscendenteAunqueLaEntradaNoEsteOrdenada() {
        val porVencer = listOf(
            documento(1, "Día 60", "2027-01-07"),
            documento(2, "Día 30", "2026-12-08"),
            documento(3, "Hoy", "2026-11-08")
        )

        val resultado = Reportes.documentosPorVencer(porVencer, hoy)

        assertEquals(listOf("Hoy", "Día 30", "Día 60"), resultado.map { it.titulo })
    }

    @Test
    fun resumenVencimientosTotalReflejaElTamañoDeLaEntradaYCuntaFechasInvalidasComoSinVencimiento() {
        val entrada = listOf(
            documento(1, "Válido", "2026-11-20"),
            documento(2, "Nulo", null),
            documento(3, "En blanco", "   "),
            documento(4, "Mal formado", "2026-13-40"),
            documento(5, "Formato incorrecto", "08/11/2026")
        )

        val resumen = Reportes.resumenVencimientos(entrada, hoy)

        assertEquals(entrada.size, resumen.total)
        assertEquals(4, resumen.sinVencimiento)
        assertEquals(1, resumen.porVencer30)
        assertEquals(0, resumen.vencidos)
        assertEquals(0, resumen.porVencer60)
    }

    @Test
    fun indicadoresInicializanTodosLosEstadosYCategorias() {
        val indicadores = Reportes.indicadores(documentos, hoy)

        assertEquals(4, indicadores.total)
        assertEquals(EstadoDocumento.entries.size, indicadores.porEstado.size)
        assertEquals(CategoriaDocumento.entries.size, indicadores.porCategoria.size)
        assertEquals(3, indicadores.porEstado[EstadoDocumento.VIGENTE])
        assertEquals(1, indicadores.porEstado[EstadoDocumento.PENDIENTE_REVISION])
        assertEquals(0, indicadores.porEstado[EstadoDocumento.ARCHIVADO])
        assertEquals(3, indicadores.porCategoria[CategoriaDocumento.CONTRATO])
        assertEquals(1, indicadores.porCategoria[CategoriaDocumento.PERMISO])
        assertEquals(1, indicadores.vencimientos.vencidos)
    }
}
