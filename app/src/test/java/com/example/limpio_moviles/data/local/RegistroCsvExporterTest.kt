package com.example.limpio_moviles.data.local

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RegistroCsvExporterTest {

    private companion object {
        const val BOM = "\uFEFF"
        const val CRLF = "\r\n"
        const val CABECERA =
            "id,alias,ambito,tipo,intensidad,contexto,estrategia,nota,objetivo,fecha_hora_iso,fecha_hora_millis,sincronizado"
        const val FECHA_FIJA = 1_700_000_000_000L
    }

    private fun registro(
        id: Int = 1,
        usuarioAlias: String = "ana",
        ambito: String = "ANIMO",
        tipo: String = "Feliz",
        intensidad: Int = 4,
        contexto: String = "Casa",
        estrategia: String = "Respiración",
        nota: String = "Sin novedad",
        estadoObjetivo: String = "Mantener rutina",
        fechaHora: Long = FECHA_FIJA,
        sincronizado: Boolean = true
    ) = RegistroEntity(
        id = id,
        usuarioAlias = usuarioAlias,
        ambito = ambito,
        tipo = tipo,
        intensidad = intensidad,
        contexto = contexto,
        estrategia = estrategia,
        nota = nota,
        estadoObjetivo = estadoObjetivo,
        fechaHora = fechaHora,
        sincronizado = sincronizado
    )

    @Test
    fun salidaComienzaConBomYContieneLaCabeceraExacta() {
        // Arrange
        val registros = listOf(registro())

        // Act
        val contenido = RegistroCsvExporter.generar(registros)

        // Assert
        assertTrue("La salida debe comenzar con el BOM UTF-8", contenido.startsWith(BOM))
        assertTrue(
            "La cabecera debe aparecer inmediatamente después del BOM",
            contenido.substring(BOM.length).startsWith("$CABECERA$CRLF")
        )
    }

    @Test
    fun registroCompletoGeneraLaFilaConTodosLosCampos() {
        // Arrange
        val entidad = RegistroEntity(
            id = 42,
            usuarioAlias = "ana",
            ambito = "ANIMO",
            tipo = "Feliz",
            intensidad = 4,
            contexto = "Casa",
            estrategia = "Respiración",
            nota = "Sin novedad",
            estadoObjetivo = "Mantener rutina",
            fechaHora = FECHA_FIJA,
            sincronizado = true
        )
        val fechaIso = Instant.ofEpochMilli(FECHA_FIJA).toString()
        val filaEsperada =
            "42,ana,ANIMO,Feliz,4,Casa,Respiración,Sin novedad,Mantener rutina,$fechaIso,$FECHA_FIJA,true"

        // Act
        val contenido = RegistroCsvExporter.generar(listOf(entidad))

        // Assert
        assertEquals("$BOM$CABECERA$CRLF$filaEsperada$CRLF", contenido)
    }

    @Test
    fun campoConComaComillasYSaltoDeLineaSeEscapaConComillasDobladas() {
        // Arrange
        val nota = "Coma, comillas \"dobles\" y\nsalto"
        val campoEsperado = "\"Coma, comillas \"\"dobles\"\" y\nsalto\""
        val registros = listOf(registro(nota = nota))

        // Act
        val contenido = RegistroCsvExporter.generar(registros)

        // Assert
        assertTrue(
            "El campo escapado debe aparecer exactamente entre comillas con las comillas internas dobladas",
            contenido.contains(campoEsperado)
        )
    }

    @Test
    fun listaVaciaProduceSoloBomCabeceraYSaltoCrlf() {
        // Arrange
        val registros = emptyList<RegistroEntity>()

        // Act
        val contenido = RegistroCsvExporter.generar(registros)

        // Assert
        assertEquals("$BOM$CABECERA$CRLF", contenido)
    }

    @Test
    fun nombreArchivoConMillisFijosCumpleElPatronEsperado() {
        // Arrange
        val patron = Regex("""lemac_datalab_\d{8}_\d{6}\.csv""")

        // Act
        val nombre = RegistroCsvExporter.nombreArchivo(FECHA_FIJA)

        // Assert
        assertTrue(
            "El nombre '$nombre' debe cumplir el patrón lemac_datalab_yyyyMMdd_HHmmss.csv",
            patron.matches(nombre)
        )
    }

    @Test
    fun campoQueEmpiezaComoFormulaSeNeutralizaConApostrofo() {
        // Arrange
        val registros = listOf(registro(nota = "=SUM(A1:A2)"))

        // Act
        val contenido = RegistroCsvExporter.generar(registros)

        // Assert
        assertTrue(
            "Un texto que empieza por = debe llevar un apostrofo para que la hoja no lo evalue",
            contenido.contains("'=SUM(A1:A2)")
        )
    }

    @Test
    fun retornoDeCarroAisladoSeEscapaEntreComillas() {
        // Arrange
        val registros = listOf(registro(nota = "linea\rretorno"))
        val campoEsperado = "\"linea\rretorno\""

        // Act
        val contenido = RegistroCsvExporter.generar(registros)

        // Assert
        assertTrue(
            "Un campo con retorno de carro debe ir entre comillas",
            contenido.contains(campoEsperado)
        )
    }

    @Test
    fun lasFilasUsanSaltosDeLineaCrlf() {
        // Arrange
        val registros = listOf(registro(id = 1), registro(id = 2))

        // Act
        val contenido = RegistroCsvExporter.generar(registros)

        // Assert
        assertTrue("La cabecera debe terminar con CRLF", contenido.substring(BOM.length).startsWith("$CABECERA$CRLF"))
        assertTrue("La salida debe terminar con CRLF", contenido.endsWith(CRLF))
        assertFalse(
            "No debe haber saltos de línea sueltos (solo CRLF)",
            contenido.replace(CRLF, "").contains("\n")
        )
        // Cabecera + 2 filas + cadena vacía final tras el último CRLF
        assertEquals(4, contenido.split(CRLF).size)
    }
}
