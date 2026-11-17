package com.example.gestiondocumental.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas de regresion de [ApiConfig].
 *
 * Blindan la URL base que usa Retrofit: desde el emulador Android el PC
 * anfitrion es `10.0.2.2`, no el loopback del propio telefono.
 */
class ApiConfigTest {

    @Test
    fun baseUrlEsExactamenteLaDelEmulador() {
        // Arrange
        val esperada = "http://10.0.2.2:8000/"

        // Act
        val baseUrl = ApiConfig.BASE_URL

        // Assert
        assertEquals(esperada, baseUrl)
    }

    @Test
    fun baseUrlTerminaEnBarraPorqueRetrofitLaLoExige() {
        // Arrange
        val baseUrl = ApiConfig.BASE_URL

        // Act
        val terminaEnBarra = baseUrl.endsWith("/")

        // Assert
        assertTrue("La BASE_URL de Retrofit debe terminar en '/', fue: $baseUrl", terminaEnBarra)
    }

    @Test
    fun endpointDeDocumentosEsElEsperado() {
        // Arrange
        val esperado = "documentos"

        // Act
        val endpoint = ApiConfig.ENDPOINT_DOCUMENTOS

        // Assert
        assertEquals(esperado, endpoint)
    }

    @Test
    fun baseUrlCompuestaConElEndpointFormaLaRutaDeDocumentos() {
        // Arrange
        val esperada = "http://10.0.2.2:8000/documentos"

        // Act
        val urlDocumentos = ApiConfig.BASE_URL + ApiConfig.ENDPOINT_DOCUMENTOS

        // Assert
        assertEquals(esperada, urlDocumentos)
    }

    @Test
    fun baseUrlNoUsaLoopbackDelEquipo() {
        // Arrange
        val baseUrl = ApiConfig.BASE_URL

        // Act
        val contieneLoopback = baseUrl.contains("127.0.0.1")
        val contieneLocalhost = baseUrl.contains("localhost")

        // Assert
        assertFalse(
            "El loopback del telefono no es el PC anfitrion; usar 10.0.2.2 en el emulador. Fue: $baseUrl",
            contieneLoopback
        )
        assertFalse("La BASE_URL no debe usar 'localhost'. Fue: $baseUrl", contieneLocalhost)
    }
}
