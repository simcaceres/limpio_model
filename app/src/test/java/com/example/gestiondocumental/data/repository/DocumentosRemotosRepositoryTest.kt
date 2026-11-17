package com.example.gestiondocumental.data.repository

import com.example.gestiondocumental.data.remote.DocumentosApiService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Pruebas de integracion del repositorio remoto contra un MockWebServer real.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DocumentosRemotosRepositoryTest {

    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun devuelveResultadoExitosoOrdenadoPorIdYConsultaLaRutaDocumentos() = runTest {
        // Arrange
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(JSON_TRES_DOCUMENTOS_DESORDENADOS)
        )
        val repositorio = crearRepositorio(StandardTestDispatcher(testScheduler))

        // Act
        val resultado = repositorio.obtenerDocumentos()

        // Assert
        assertTrue("Se esperaba exito, fue: $resultado", resultado.isSuccess)
        val documentos = resultado.getOrThrow()
        assertEquals(3, documentos.size)
        assertEquals(listOf(3L, 7L, 12L), documentos.map { it.id })
        assertEquals("/documentos", server.takeRequest().path)
    }

    @Test
    fun devuelveFailureConHttpExceptionCuandoElServidorResponde500() = runTest {
        // Arrange
        server.enqueue(MockResponse().setResponseCode(500))
        val repositorio = crearRepositorio(StandardTestDispatcher(testScheduler))

        // Act
        val resultado = repositorio.obtenerDocumentos()

        // Assert
        assertTrue("Se esperaba failure, fue: $resultado", resultado.isFailure)
        val causa = resultado.exceptionOrNull()
        assertTrue("La causa debe ser HttpException, fue: $causa", causa is HttpException)
        assertEquals(500, (causa as HttpException).code())
    }

    @Test
    fun devuelveFailureCuandoElJsonEstaMalformado() = runTest {
        // Arrange
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{no-json")
        )
        val repositorio = crearRepositorio(StandardTestDispatcher(testScheduler))

        // Act
        val resultado = repositorio.obtenerDocumentos()

        // Assert
        assertTrue("Se esperaba failure, fue: $resultado", resultado.isFailure)
    }

    @Test
    fun devuelveFailureConMensajeDeCategoriaCuandoElEnumEsInvalido() = runTest {
        // Arrange
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(JSON_CATEGORIA_INVALIDA)
        )
        val repositorio = crearRepositorio(StandardTestDispatcher(testScheduler))

        // Act
        val resultado = repositorio.obtenerDocumentos()

        // Assert
        assertTrue("Se esperaba failure, fue: $resultado", resultado.isFailure)
        val mensaje = resultado.exceptionOrNull()?.message.orEmpty()
        assertTrue(
            "El mensaje debe contener 'Categoria desconocida', fue: $mensaje",
            mensaje.contains("Categoria desconocida")
        )
    }

    private fun crearRepositorio(dispatcher: CoroutineDispatcher): DocumentosRemotosRepository {
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DocumentosApiService::class.java)
        return DocumentosRemotosRepository(api = api, dispatcher = dispatcher)
    }

    private companion object {
        const val JSON_TRES_DOCUMENTOS_DESORDENADOS = """
            [
              {
                "id": 12,
                "usuarioId": 2,
                "propietario": "Ana Perez",
                "categoria": "REPORTE",
                "titulo": "Reporte anual",
                "estado": "ARCHIVADO",
                "versionActual": 1,
                "fechaVencimiento": null,
                "dependencia": "Recursos Humanos"
              },
              {
                "id": 3,
                "usuarioId": 4,
                "propietario": "Luis Soto",
                "categoria": "CONTRATO",
                "titulo": "Contrato de trabajo",
                "estado": "VIGENTE",
                "versionActual": 2,
                "fechaVencimiento": "2027-01-31",
                "dependencia": "Recursos Humanos",
                "archivoNombre": "contrato.pdf"
              },
              {
                "id": 7,
                "usuarioId": 6,
                "propietario": "Marta Rojas",
                "categoria": "CERTIFICADO",
                "titulo": "Certificado laboral",
                "estado": "APROBADO",
                "versionActual": 1,
                "fechaVencimiento": null,
                "dependencia": "Recursos Humanos"
              }
            ]
        """

        const val JSON_CATEGORIA_INVALIDA = """
            [
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
            ]
        """
    }
}
