package com.example.gestiondocumental.model

import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

/** Resumen de vencimientos documentales calculado a una fecha de referencia. */
data class ResumenVencimientos(
    val vencidos: Int,
    val porVencer30: Int,
    val porVencer60: Int,
    val sinVencimiento: Int,
    val total: Int
)

/** Indicadores agregados del expediente documental. */
data class IndicadoresDocumentales(
    val total: Int,
    val porEstado: Map<EstadoDocumento, Int>,
    val porCategoria: Map<CategoriaDocumento, Int>,
    val vencimientos: ResumenVencimientos
)

/**
 * Cálculos puros de reportes (sin Android) para poder probarlos con JUnit.
 *
 * Las fechas de vencimiento se almacenan en formato ISO `yyyy-MM-dd`. Cualquier
 * valor nulo, en blanco o no parseable se considera "sin vencimiento".
 */
object Reportes {

    private const val DIAS_POR_VENCER_CORTO = 30L
    private const val DIAS_POR_VENCER_LARGO = 60L

    /** Días entre [hoy] y [fecha]. Devuelve null si la fecha no es válida. */
    fun diasHastaVencimiento(fecha: String?, hoy: LocalDate): Long? {
        val texto = fecha?.trim()
        if (texto.isNullOrBlank()) return null
        val vencimiento = try {
            LocalDate.parse(texto)
        } catch (_: DateTimeParseException) {
            return null
        }
        return ChronoUnit.DAYS.between(hoy, vencimiento)
    }

    /** Cuenta documentos vencidos, por vencer (30/60 días) y sin vencimiento. */
    fun resumenVencimientos(documentos: List<Documento>, hoy: LocalDate): ResumenVencimientos {
        var vencidos = 0
        var porVencer30 = 0
        var porVencer60 = 0
        var sinVencimiento = 0

        documentos.forEach { documento ->
            val dias = diasHastaVencimiento(documento.fechaVencimiento, hoy)
            when {
                dias == null -> sinVencimiento++
                dias < 0 -> vencidos++
                dias <= DIAS_POR_VENCER_CORTO -> porVencer30++
                dias <= DIAS_POR_VENCER_LARGO -> porVencer60++
            }
        }

        return ResumenVencimientos(
            vencidos = vencidos,
            porVencer30 = porVencer30,
            porVencer60 = porVencer60,
            sinVencimiento = sinVencimiento,
            total = documentos.size
        )
    }

    /** Indicadores por estado, categoría y vencimientos para el panel de reportes. */
    fun indicadores(documentos: List<Documento>, hoy: LocalDate): IndicadoresDocumentales {
        val porEstado = EstadoDocumento.entries.associateWith { 0 }.toMutableMap()
        val porCategoria = CategoriaDocumento.entries.associateWith { 0 }.toMutableMap()

        documentos.forEach { documento ->
            porEstado[documento.estado] = (porEstado[documento.estado] ?: 0) + 1
            porCategoria[documento.categoria] = (porCategoria[documento.categoria] ?: 0) + 1
        }

        return IndicadoresDocumentales(
            total = documentos.size,
            porEstado = porEstado,
            porCategoria = porCategoria,
            vencimientos = resumenVencimientos(documentos, hoy)
        )
    }

    /**
     * Documentos que vencen entre hoy y [dias] (inclusive). Excluye vencidos y
     * documentos sin fecha, ordenados de forma ascendente por vencimiento.
     */
    fun documentosPorVencer(
        documentos: List<Documento>,
        hoy: LocalDate,
        dias: Long = DIAS_POR_VENCER_LARGO
    ): List<Documento> = documentos
        .mapNotNull { documento ->
            val restantes = diasHastaVencimiento(documento.fechaVencimiento, hoy)
            if (restantes != null && restantes in 0..dias) documento else null
        }
        .sortedBy { it.fechaVencimiento }
}
