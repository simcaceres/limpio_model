package com.example.gestiondocumental.viewmodel

import com.example.gestiondocumental.data.remote.DocumentoDto
import com.example.gestiondocumental.data.remote.DocumentosApiService
import com.example.gestiondocumental.data.repository.DocumentosRemotosRepository
import com.example.gestiondocumental.model.DocumentosRemotosUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/**
 * Pruebas del ViewModel de documentos remotos con un fake de la API.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DocumentosRemotosViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun alCrearElViewModelElEstadoInicialEsCargandoYLuegoDatos() = runTest(dispatcher) {
        // Arrange
        val fake = FakeDocumentosApi { listOf(DTO_UNO, DTO_DOS) }
        val viewModel = crearViewModel(fake)

        // Assert estado inicial
        assertEquals(DocumentosRemotosUiState.Cargando, viewModel.estado.value)

        // Act
        advanceUntilIdle()

        // Assert estado final
        val estado = viewModel.estado.value
        assertTrue("Se esperaba Datos, fue: $estado", estado is DocumentosRemotosUiState.Datos)
        val documentos = (estado as DocumentosRemotosUiState.Datos).documentos
        assertEquals(listOf(1L, 2L), documentos.map { it.id })
    }

    @Test
    fun errorDeRedSeTraduceEnEstadoErrorConMensajeDeConexion() = runTest(dispatcher) {
        // Arrange
        val fake = FakeDocumentosApi { throw IOException("sin red") }
        val viewModel = crearViewModel(fake)

        // Act
        advanceUntilIdle()

        // Assert
        val estado = viewModel.estado.value
        assertTrue("Se esperaba Error, fue: $estado", estado is DocumentosRemotosUiState.Error)
        val mensaje = (estado as DocumentosRemotosUiState.Error).mensaje
        assertTrue(
            "El mensaje debe contener 'No se pudo conectar', fue: $mensaje",
            mensaje.contains("No se pudo conectar")
        )
    }

    @Test
    fun reintentarTrasErrorVuelveAEstadoDatosYVuelveALlamarLaApi() = runTest(dispatcher) {
        // Arrange: primero falla, luego responde datos
        var debeFallar = true
        val fake = FakeDocumentosApi {
            if (debeFallar) throw IOException("sin red") else listOf(DTO_UNO)
        }
        val viewModel = crearViewModel(fake)
        advanceUntilIdle()
        assertTrue(viewModel.estado.value is DocumentosRemotosUiState.Error)

        // Act: el fake ahora responde correctamente y se reintenta
        debeFallar = false
        viewModel.cargar()
        advanceUntilIdle()

        // Assert
        val estado = viewModel.estado.value
        assertTrue("Se esperaba Datos, fue: $estado", estado is DocumentosRemotosUiState.Datos)
        assertEquals(listOf(1L), (estado as DocumentosRemotosUiState.Datos).documentos.map { it.id })
        assertEquals(2, fake.llamadas)
    }

    @Test
    fun errorHttpSeTraduceEnEstadoErrorConElCodigoDeEstado() = runTest(dispatcher) {
        // Arrange
        val response = Response.error<Any>(
            503,
            "servicio no disponible".toResponseBody("text/plain".toMediaType())
        )
        val fake = FakeDocumentosApi { throw HttpException(response) }
        val viewModel = crearViewModel(fake)

        // Act
        advanceUntilIdle()

        // Assert
        val estado = viewModel.estado.value
        assertTrue("Se esperaba Error, fue: $estado", estado is DocumentosRemotosUiState.Error)
        val mensaje = (estado as DocumentosRemotosUiState.Error).mensaje
        assertTrue("El mensaje debe contener '503', fue: $mensaje", mensaje.contains("503"))
    }

    private fun crearViewModel(fake: FakeDocumentosApi): DocumentosRemotosViewModel {
        val repositorio = DocumentosRemotosRepository(
            api = fake,
            dispatcher = StandardTestDispatcher(dispatcher.scheduler)
        )
        return DocumentosRemotosViewModel(repositorio)
    }

    private class FakeDocumentosApi(
        var respuesta: () -> List<DocumentoDto>
    ) : DocumentosApiService {

        var llamadas: Int = 0

        override suspend fun obtenerDocumentos(): List<DocumentoDto> {
            llamadas++
            return respuesta()
        }
    }

    private companion object {
        val DTO_UNO = DocumentoDto(
            id = 1L,
            usuarioId = 10L,
            propietario = "Ana Perez",
            categoria = "CONTRATO",
            titulo = "Contrato de trabajo",
            estado = "VIGENTE",
            versionActual = 1,
            fechaVencimiento = "2027-01-31",
            dependencia = "Recursos Humanos",
            archivoNombre = "contrato.pdf"
        )

        val DTO_DOS = DocumentoDto(
            id = 2L,
            usuarioId = 11L,
            propietario = "Luis Soto",
            categoria = "CERTIFICADO",
            titulo = "Certificado laboral",
            estado = "APROBADO",
            versionActual = 1,
            fechaVencimiento = null,
            dependencia = "Recursos Humanos",
            archivoNombre = null
        )
    }
}
