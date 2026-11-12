package com.example.limpio_moviles.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import com.example.limpio_moviles.data.local.RegistroDao
import com.example.limpio_moviles.data.local.RegistroEntity
import com.example.limpio_moviles.data.repository.PerfilRepository
import com.example.limpio_moviles.data.repository.RegistroRepository
import com.example.limpio_moviles.model.Ambito
import com.example.limpio_moviles.util.ExportadorArchivo
import com.example.limpio_moviles.util.FotoPerfilStorage
import com.example.limpio_moviles.util.RecordatorioProgramador
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegistroViewModelTest {

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
    fun guardarCrearRegistroUsaRelojYGuardaSincronizadoConMensajeDeExito() = runTest(dispatcher) {
        // Arrange
        val dao = FakeRegistroDao()
        val relojFijo = 1_700_000_123_000L
        val viewModel = crearViewModel(dao = dao, clock = { relojFijo })
        viewModel.actualizarTipo("Rutina")
        viewModel.actualizarEstrategia("Lista de pasos")

        // Act
        viewModel.guardar()
        advanceUntilIdle()

        // Assert
        val guardado = dao.registros.value.single()
        assertNotEquals(0, guardado.id)
        assertEquals(relojFijo, guardado.fechaHora)
        assertFalse("Un registro nuevo debe quedar pendiente de sincronizar", guardado.sincronizado)
        assertEquals("ana", guardado.usuarioAlias)
        assertEquals("Registro guardado", viewModel.uiState.mensaje)
        assertTrue(viewModel.uiState.guardadoExitoso)
        assertFalse(viewModel.uiState.cargando)
        assertEquals(1, dao.insertarLlamadas)
        assertEquals(0, dao.actualizarLlamadas)
    }

    @Test
    fun guardarEdicionConservaFechaHoraOriginalYActualizaConMensaje() = runTest(dispatcher) {
        // Arrange
        val existente = registro(
            id = 5,
            tipo = "Rutina",
            estrategia = "Lista de pasos",
            fechaHora = 1_000L
        )
        val dao = FakeRegistroDao(listOf(existente))
        val viewModel = crearViewModel(dao = dao, clock = { 9_999L })

        viewModel.prepararFormulario(Ambito.NEURODESARROLLO, existente.id)
        advanceUntilIdle()
        viewModel.actualizarTipo("Rutina editada")

        // Act
        viewModel.guardar()
        advanceUntilIdle()

        // Assert
        val actualizado = dao.registros.value.single()
        assertEquals("Rutina editada", actualizado.tipo)
        assertEquals(1_000L, actualizado.fechaHora)
        assertFalse("Una edición debe quedar pendiente de sincronizar", actualizado.sincronizado)
        assertEquals("Registro actualizado", viewModel.uiState.mensaje)
        assertTrue(viewModel.uiState.guardadoExitoso)
        assertEquals(0, dao.insertarLlamadas)
        assertEquals(1, dao.actualizarLlamadas)
    }

    @Test
    fun guardarEdicionDeRegistroInexistenteMuestraMensajeSinInsertarNiActualizar() = runTest(dispatcher) {
        // Arrange
        val existente = registro(
            id = 7,
            tipo = "Rutina",
            estrategia = "Lista de pasos"
        )
        val dao = FakeRegistroDao(listOf(existente))
        val viewModel = crearViewModel(dao = dao)

        viewModel.prepararFormulario(Ambito.NEURODESARROLLO, existente.id)
        advanceUntilIdle()
        dao.registros.value = emptyList()

        // Act
        viewModel.guardar()
        advanceUntilIdle()

        // Assert
        assertEquals(
            "No se encontró el registro que intentas editar",
            viewModel.uiState.mensaje
        )
        assertFalse(viewModel.uiState.guardadoExitoso)
        assertFalse(viewModel.uiState.cargando)
        assertTrue(dao.registros.value.isEmpty())
        assertEquals(0, dao.insertarLlamadas)
        assertEquals(0, dao.actualizarLlamadas)
    }

    @Test
    fun guardarConErroresDeValidacionNoLlamaAlRepositorioYExponeErrores() = runTest(dispatcher) {
        // Arrange
        val dao = FakeRegistroDao()
        val viewModel = crearViewModel(dao = dao)

        // Act
        viewModel.guardar()
        advanceUntilIdle()

        // Assert
        assertTrue(viewModel.uiState.errores.containsKey("tipo"))
        assertTrue(viewModel.uiState.errores.containsKey("estrategia"))
        assertFalse(viewModel.uiState.guardadoExitoso)
        assertEquals(0, dao.insertarLlamadas)
        assertEquals(0, dao.actualizarLlamadas)
        assertTrue(dao.registros.value.isEmpty())
    }

    @Test
    fun cargarDemoMuestraMensajeDeExitoCuandoSeInsertanDatos() = runTest(dispatcher) {
        // Arrange
        val dao = FakeRegistroDao()
        val viewModel = crearViewModel(
            dao = dao,
            perfil = FakePerfilRepository(alias = "ana")
        )

        // Act
        viewModel.cargarDemo()
        advanceUntilIdle()

        // Assert
        assertEquals("Datos de demostración cargados", viewModel.uiState.mensaje)
        assertEquals(4, dao.registros.value.size)
        assertFalse(viewModel.uiState.cargando)
    }

    @Test
    fun cargarDemoMuestraMensajeCuandoYaHayRegistros() = runTest(dispatcher) {
        // Arrange
        val dao = FakeRegistroDao(listOf(registro(id = 1, tipo = "Existente")))
        val viewModel = crearViewModel(dao = dao)

        // Act
        viewModel.cargarDemo()
        advanceUntilIdle()

        // Assert
        assertEquals(
            "Ya hay registros; no se duplicaron los datos de ejemplo",
            viewModel.uiState.mensaje
        )
        assertEquals(1, dao.registros.value.size)
        assertFalse(viewModel.uiState.cargando)
    }

    @Test
    fun cargarDemoMuestraMensajeDeErrorCuandoFallaElRepositorio() = runTest(dispatcher) {
        // Arrange
        val dao = FakeRegistroDao().apply { fallarAlContar = true }
        val viewModel = crearViewModel(dao = dao)

        // Act
        viewModel.cargarDemo()
        advanceUntilIdle()

        // Assert
        assertEquals(
            "No se pudieron cargar los datos de demostración",
            viewModel.uiState.mensaje
        )
        assertFalse(viewModel.uiState.cargando)
    }

    private fun crearViewModel(
        dao: FakeRegistroDao,
        perfil: FakePerfilRepository = FakePerfilRepository(),
        clock: () -> Long = { 1_700_000_000_000L }
    ) = RegistroViewModel(
        registroRepository = RegistroRepository(dao),
        perfilRepository = perfil,
        fotoPerfilStorage = FakeFotoPerfilStorage(),
        recordatorioProgramador = FakeRecordatorioProgramador(),
        exportadorArchivo = FakeExportadorArchivo(),
        clock = clock
    )

    private fun registro(
        id: Int = 1,
        tipo: String = "Rutina",
        estrategia: String = "Lista de pasos",
        fechaHora: Long = 1_700_000_000_000L
    ) = RegistroEntity(
        id = id,
        usuarioAlias = "ana",
        ambito = Ambito.NEURODESARROLLO.name,
        tipo = tipo,
        intensidad = 3,
        contexto = "Casa",
        estrategia = estrategia,
        nota = "Sin novedad",
        estadoObjetivo = "Mantener rutina",
        fechaHora = fechaHora,
        sincronizado = true
    )

    private class FakeRegistroDao(
        inicial: List<RegistroEntity> = emptyList()
    ) : RegistroDao {

        val registros = MutableStateFlow(inicial)
        var insertarLlamadas = 0
        var actualizarLlamadas = 0
        var fallarAlContar = false

        override fun observarTodos(): Flow<List<RegistroEntity>> = registros

        override fun observarPorAmbito(ambito: String): Flow<List<RegistroEntity>> =
            registros.map { lista -> lista.filter { it.ambito == ambito } }

        override suspend fun obtenerPorId(id: Int): RegistroEntity? =
            registros.value.firstOrNull { it.id == id }

        override suspend fun obtenerTodos(): List<RegistroEntity> =
            registros.value.sortedWith(
                compareByDescending<RegistroEntity> { it.fechaHora }.thenByDescending { it.id }
            )

        override suspend fun obtenerPendientes(): List<RegistroEntity> =
            registros.value.filter { !it.sincronizado }

        override suspend fun insertar(registro: RegistroEntity): Long {
            insertarLlamadas++
            val id = if (registro.id == 0) {
                (registros.value.maxOfOrNull { it.id } ?: 0) + 1
            } else {
                registro.id
            }
            registros.value = registros.value + registro.copy(id = id)
            return id.toLong()
        }

        override suspend fun actualizar(registro: RegistroEntity): Int {
            actualizarLlamadas++
            val indice = registros.value.indexOfFirst { it.id == registro.id }
            if (indice < 0) return 0
            registros.value = registros.value.toMutableList().also { it[indice] = registro }
            return 1
        }

        override suspend fun eliminar(registro: RegistroEntity) {
            registros.value = registros.value.filterNot { it.id == registro.id }
        }

        override suspend fun marcarSincronizados(ids: List<Int>) {
            registros.value = registros.value.map { registro ->
                if (registro.id in ids) registro.copy(sincronizado = true) else registro
            }
        }

        override suspend fun eliminarTodos() {
            registros.value = emptyList()
        }

        override suspend fun contar(): Int {
            if (fallarAlContar) error("Fallo simulado de la base de datos")
            return registros.value.size
        }
    }

    private class FakePerfilRepository(
        var alias: String = "ana",
        var fotoPath: String? = null,
        var recordatorio: Boolean = false
    ) : PerfilRepository {

        override fun cargarAlias(): String = alias

        override fun guardarAlias(alias: String) {
            this.alias = alias
        }

        override fun cargarFotoPath(): String? = fotoPath

        override fun guardarFotoPath(path: String?) {
            fotoPath = path
        }

        override fun cargarRecordatorio(): Boolean = recordatorio

        override fun guardarRecordatorio(activo: Boolean) {
            recordatorio = activo
        }
    }

    private class FakeFotoPerfilStorage : FotoPerfilStorage {

        override fun cargar(path: String?): Bitmap? = null

        override fun guardar(bitmap: Bitmap): String? = "/tmp/perfil.png"
    }

    private class FakeRecordatorioProgramador : RecordatorioProgramador {

        override fun programar() = Unit

        override fun cancelar() = Unit
    }

    private class FakeExportadorArchivo : ExportadorArchivo {

        override suspend fun escribir(uri: Uri, contenido: String): Boolean = true
    }
}
