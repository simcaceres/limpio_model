package com.example.limpio_moviles.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RegistroValidatorTest {

    private fun estadoNeurodesarrollo(
        tipo: String = "Rutina",
        intensidad: Int = 3,
        estrategia: String = "Respiración diafragmática",
        nota: String = ""
    ) = RegistroUiState(
        ambito = Ambito.NEURODESARROLLO,
        tipo = tipo,
        intensidad = intensidad,
        contexto = "Casa",
        estrategia = estrategia,
        nota = nota
    )

    @Test
    fun formularioValidoParaNeurodesarrolloNoProduceErrores() {
        // Arrange
        val estado = estadoNeurodesarrollo()

        // Act
        val errores = RegistroValidator.validar(estado)

        // Assert
        assertTrue("No se esperaban errores, pero se obtuvo: $errores", errores.isEmpty())
    }

    @Test
    fun tipoEnBlancoProduceErrorEnLaClaveTipo() {
        // Arrange
        val estado = estadoNeurodesarrollo(tipo = "   ")

        // Act
        val errores = RegistroValidator.validar(estado)

        // Assert
        assertTrue("Se esperaba un error en la clave 'tipo'", errores.containsKey("tipo"))
        assertEquals("Selecciona un tipo", errores["tipo"])
    }

    @Test
    fun intensidadFueraDeRangoParaAnimoProduceErrorEnIntensidad() {
        // Arrange: ANIMO usa el rango 1..5
        val estado = RegistroUiState(
            ambito = Ambito.ANIMO,
            tipo = "Feliz",
            intensidad = 6
        )

        // Act
        val errores = RegistroValidator.validar(estado)

        // Assert
        assertTrue("Se esperaba un error en la clave 'intensidad'", errores.containsKey("intensidad"))
        assertEquals(
            "La intensidad debe estar entre 1 y 5",
            errores["intensidad"]
        )
    }

    @Test
    fun intensidadValidaParaAdiccionesNoProduceError() {
        // Arrange: ADICCIONES usa el rango 0..10
        val estado = RegistroUiState(
            ambito = Ambito.ADICCIONES,
            tipo = "Craving",
            intensidad = 0,
            contexto = "Estrés laboral"
        )

        // Act
        val errores = RegistroValidator.validar(estado)

        // Assert
        assertFalse("No se esperaba error en 'intensidad'", errores.containsKey("intensidad"))
    }

    @Test
    fun adiccionesExigeContexto() {
        // Arrange
        val estado = RegistroUiState(
            ambito = Ambito.ADICCIONES,
            tipo = "Craving",
            intensidad = 5,
            contexto = ""
        )

        // Act
        val errores = RegistroValidator.validar(estado)

        // Assert
        assertTrue("Se esperaba un error en la clave 'contexto'", errores.containsKey("contexto"))
        assertEquals("Indica el desencadenante", errores["contexto"])
    }

    @Test
    fun dbtExigeEstrategia() {
        // Arrange
        val estado = RegistroUiState(
            ambito = Ambito.DBT,
            tipo = "Mindfulness",
            intensidad = 3,
            estrategia = "  "
        )

        // Act
        val errores = RegistroValidator.validar(estado)

        // Assert
        assertTrue("Se esperaba un error en la clave 'estrategia'", errores.containsKey("estrategia"))
        assertEquals("Indica la habilidad practicada", errores["estrategia"])
    }

    @Test
    fun neurodesarrolloExigeEstrategia() {
        // Arrange
        val estado = estadoNeurodesarrollo(estrategia = "")

        // Act
        val errores = RegistroValidator.validar(estado)

        // Assert
        assertTrue("Se esperaba un error en la clave 'estrategia'", errores.containsKey("estrategia"))
        assertEquals("Indica la estrategia utilizada", errores["estrategia"])
    }

    @Test
    fun notaDeMasDe200CaracteresProduceErrorEnNota() {
        // Arrange: 201 caracteres
        val estado = estadoNeurodesarrollo(nota = "a".repeat(201))

        // Act
        val errores = RegistroValidator.validar(estado)

        // Assert
        assertTrue("Se esperaba un error en la clave 'nota'", errores.containsKey("nota"))
        assertEquals("La nota no puede superar 200 caracteres", errores["nota"])
    }

    @Test
    fun notaDeExactamente200CaracteresNoProduceError() {
        // Arrange
        val estado = estadoNeurodesarrollo(nota = "a".repeat(200))

        // Act
        val errores = RegistroValidator.validar(estado)

        // Assert
        assertFalse("No se esperaba error en 'nota'", errores.containsKey("nota"))
    }
}
