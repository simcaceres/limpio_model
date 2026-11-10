package com.example.gestiondocumental.util

import org.junit.Assert.assertEquals
import org.junit.Test

/** Pruebas puras de la detección de MIME por extensión (sin Android). */
class ArchivosMimeTest {

    @Test
    fun mimeDeDetectaPdf() {
        assertEquals("application/pdf", mimeDe("informe.pdf"))
    }

    @Test
    fun mimeDeDetectaImagenesJpgYJpeg() {
        assertEquals("image/jpeg", mimeDe("foto.jpg"))
        assertEquals("image/jpeg", mimeDe("foto.jpeg"))
    }

    @Test
    fun mimeDeDetectaPng() {
        assertEquals("image/png", mimeDe("captura.png"))
    }

    @Test
    fun mimeDeDetectaDocumentosWord() {
        assertEquals(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            mimeDe("contrato.docx")
        )
    }

    @Test
    fun mimeDeDetectaHojasDeCalculoExcel() {
        assertEquals(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            mimeDe("planilla.xlsx")
        )
    }

    @Test
    fun mimeDeDevuelveOctetStreamParaExtensionDesconocida() {
        assertEquals("application/octet-stream", mimeDe("respaldo.zip"))
        assertEquals("application/octet-stream", mimeDe("archivo"))
        assertEquals("application/octet-stream", mimeDe(""))
    }

    @Test
    fun mimeDeIgnoraMayusculasEnLaExtension() {
        assertEquals("application/pdf", mimeDe("INFORME.PDF"))
        assertEquals("image/jpeg", mimeDe("FOTO.JPG"))
        assertEquals("image/png", mimeDe("CAPTURA.PNG"))
        assertEquals(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            mimeDe("CONTRATO.DOCX")
        )
        assertEquals(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            mimeDe("PLANILLA.XLSX")
        )
    }

    @Test
    fun mimeDeSoloConsideraLaUltimaExtension() {
        assertEquals("application/pdf", mimeDe("respaldo.tar.pdf"))
    }
}
