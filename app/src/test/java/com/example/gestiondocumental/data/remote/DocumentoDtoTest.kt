package com.example.gestiondocumental.data.remote

import com.example.gestiondocumental.model.CategoriaDocumento
import com.example.gestiondocumental.model.EstadoDocumento
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas del contrato JSON `GET /documentos` y de la conversion DTO -> dominio.
 */
class DocumentoDtoTest {

    private val gson = Gson()

    @Test
    fun deserializaYmapeaTodosLosCamposCuandoElArchivoEstaPresente() {
        // Arrange
        val dto = gson.fromJson(JSON_COMPLETO, DocumentoDto::class.java)

        // Act
        val documento = dto.aModelo()

        // Assert
        assertEquals(7L, documento.id)
        assertEquals(42L, documento.usuarioId)
        assertEquals("Ana Perez", documento.propietario)
        assertEquals("Contrato de trabajo", documento.titulo)
        assertEquals(CategoriaDocumento.CONTRATO, documento.categoria)
        assertEquals(EstadoDocumento.VIGENTE, documento.estado)
        assertEquals(3, documento.versionActual)
        assertEquals("2027-01-31", documento.fechaVencimiento)
        assertEquals("Recursos Humanos", documento.dependencia)
        assertEquals("contrato.pdf", documento.archivoNombre)
    }

    @Test
    fun deserializaArchivoNombreAusenteComoNull() {
        // Arrange
        val dto = gson.fromJson(JSON_SIN_ARCHIVO, DocumentoDto::class.java)

        // Act
        val documento = dto.aModelo()

        // Assert
        assertNull(documento.archivoNombre)
        assertEquals(9L, documento.id)
        assertEquals(CategoriaDocumento.CERTIFICADO, documento.categoria)
        assertEquals(EstadoDocumento.APROBADO, documento.estado)
    }

    @Test
    fun categoriaDesconocidaLanzaIllegalArgumentExceptionConMensajeEspecifico() {
        // Arrange
        val dto = gson.fromJson(JSON_CATEGORIA_INVALIDA, DocumentoDto::class.java)

        // Act
        val error = assertThrows(IllegalArgumentException::class.java) { dto.aModelo() }

        // Assert
        assertTrue(
            "El mensaje debe describir la categoria invalida, fue: ${error.message}",
            error.message.orEmpty().contains("Categoria desconocida")
        )
    }

    @Test
    fun estadoDesconocidoLanzaIllegalArgumentExceptionConMensajeEspecifico() {
        // Arrange
        val dto = gson.fromJson(JSON_ESTADO_INVALIDO, DocumentoDto::class.java)

        // Act
        val error = assertThrows(IllegalArgumentException::class.java) { dto.aModelo() }

        // Assert
        assertTrue(
            "El mensaje debe describir el estado invalido, fue: ${error.message}",
            error.message.orEmpty().contains("Estado desconocido")
        )
    }

    @Test
    fun identificadorCeroLanzaIllegalArgumentException() {
        // Arrange
        val dto = gson.fromJson(JSON_ID_CERO, DocumentoDto::class.java)

        // Act / Assert
        val error = assertThrows(IllegalArgumentException::class.java) { dto.aModelo() }
        assertTrue(error.message.orEmpty().contains("Identificador"))
    }

    @Test
    fun tituloVacioLanzaIllegalArgumentException() {
        // Arrange
        val dto = gson.fromJson(JSON_TITULO_VACIO, DocumentoDto::class.java)

        // Act / Assert
        val error = assertThrows(IllegalArgumentException::class.java) { dto.aModelo() }
        assertTrue(error.message.orEmpty().contains("titulo"))
    }

    private companion object {
        const val JSON_COMPLETO = """
            {
              "id": 7,
              "usuarioId": 42,
              "propietario": "Ana Perez",
              "categoria": "CONTRATO",
              "titulo": "Contrato de trabajo",
              "estado": "VIGENTE",
              "versionActual": 3,
              "fechaVencimiento": "2027-01-31",
              "dependencia": "Recursos Humanos",
              "archivoNombre": "contrato.pdf"
            }
        """

        const val JSON_SIN_ARCHIVO = """
            {
              "id": 9,
              "usuarioId": 11,
              "propietario": "Luis Soto",
              "categoria": "CERTIFICADO",
              "titulo": "Certificado laboral",
              "estado": "APROBADO",
              "versionActual": 1,
              "fechaVencimiento": null,
              "dependencia": "Recursos Humanos"
            }
        """

        const val JSON_CATEGORIA_INVALIDA = """
            {
              "id": 1,
              "usuarioId": 2,
              "propietario": "Ana Perez",
              "categoria": "CATEGORIA_QUE_NO_EXISTE",
              "titulo": "Documento",
              "estado": "VIGENTE",
              "versionActual": 1,
              "dependencia": "Recursos Humanos"
            }
        """

        const val JSON_ESTADO_INVALIDO = """
            {
              "id": 1,
              "usuarioId": 2,
              "propietario": "Ana Perez",
              "categoria": "CONTRATO",
              "titulo": "Documento",
              "estado": "ESTADO_QUE_NO_EXISTE",
              "versionActual": 1,
              "dependencia": "Recursos Humanos"
            }
        """

        const val JSON_ID_CERO = """
            {
              "id": 0,
              "usuarioId": 2,
              "propietario": "Ana Perez",
              "categoria": "CONTRATO",
              "titulo": "Documento",
              "estado": "VIGENTE",
              "versionActual": 1,
              "dependencia": "Recursos Humanos"
            }
        """

        const val JSON_TITULO_VACIO = """
            {
              "id": 1,
              "usuarioId": 2,
              "propietario": "Ana Perez",
              "categoria": "CONTRATO",
              "titulo": "   ",
              "estado": "VIGENTE",
              "versionActual": 1,
              "dependencia": "Recursos Humanos"
            }
        """
    }
}
