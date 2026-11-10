package com.example.gestiondocumental.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadoresTest {

    @Test
    fun correoValidoAceptaFormatoCorrecto() {
        assertTrue(Validadores.correoValido("ana.perez@salud.cl"))
        assertFalse(Validadores.correoValido("ana.perez"))
        assertFalse(Validadores.correoValido("ana@"))
    }

    @Test
    fun rutValidoAceptaFormatoConPuntosYGuion() {
        assertTrue(Validadores.rutValido("11.111.111-1"))
        assertTrue(Validadores.rutValido("111111111"))
        assertFalse(Validadores.rutValido("abc"))
        assertFalse(Validadores.rutValido("1"))
    }

    @Test
    fun rutCanonicoNormalizaFormato() {
        assertEquals("11111111-1", Validadores.rutCanonico("11.111.111-1"))
        assertEquals("11111111-1", Validadores.rutCanonico("11111111-1"))
        assertEquals("1234567-8", Validadores.rutCanonico("12345678"))
    }

    @Test
    fun usuarioValidoDetectaCamposFaltantes() {
        assertNull(Validadores.usuarioValido("Ana Pérez", "ana@salud.cl", "11.111.111-1"))
        assertNotNull(Validadores.usuarioValido("", "ana@salud.cl", "11.111.111-1"))
        assertNotNull(Validadores.usuarioValido("Ana", "correo-malo", "11.111.111-1"))
        assertNotNull(Validadores.usuarioValido("Ana", "ana@salud.cl", "xx"))
    }
}
