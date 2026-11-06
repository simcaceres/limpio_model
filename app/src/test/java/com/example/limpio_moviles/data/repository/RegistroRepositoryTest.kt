package com.example.limpio_moviles.data.repository

import com.example.limpio_moviles.data.local.RegistroDao
import com.example.limpio_moviles.data.local.RegistroEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RegistroRepositoryTest {

    @Test
    fun guardarInsertaUnRegistroNuevoYDevuelveTrue() = runBlocking {
        // Arrange
        val dao = FakeRegistroDao()
        val repository = RegistroRepository(dao)
        val nuevo = registro(id = 0, tipo = "Nuevo")

        // Act
        val guardado = repository.guardar(nuevo)

        // Assert
        assertTrue("Un registro nuevo debe guardarse", guardado)
        assertEquals(1, dao.registros.value.size)
        assertEquals("Nuevo", dao.registros.value.single().tipo)
    }

    @Test
    fun guardarActualizaUnRegistroExistenteYDevuelveTrue() = runBlocking {
        // Arrange
        val original = registro(id = 1, tipo = "Original", fechaHora = 1_000L)
        val dao = FakeRegistroDao(listOf(original))
        val repository = RegistroRepository(dao)

        // Act
        val guardado = repository.guardar(original.copy(tipo = "Editado"))

        // Assert
        assertTrue("Un registro existente debe actualizarse", guardado)
        assertEquals("Editado", dao.registros.value.single().tipo)
        assertEquals(1_000L, dao.registros.value.single().fechaHora)
    }

    @Test
    fun guardarDevuelveFalseSiElRegistroAEditarYaNoExiste() = runBlocking {
        // Arrange
        val dao = FakeRegistroDao()
        val repository = RegistroRepository(dao)

        // Act
        val guardado = repository.guardar(registro(id = 99, tipo = "Fantasma"))

        // Assert
        assertFalse("No debe simularse una actualización que no afectó filas", guardado)
        assertTrue(dao.registros.value.isEmpty())
    }

    @Test
    fun cargarDemoInsertaCuatroRegistrosCuandoLaBaseEstaVacia() = runBlocking {
        // Arrange
        val dao = FakeRegistroDao()
        val repository = RegistroRepository(dao)

        // Act
        val insertados = repository.cargarDemo("ana")

        // Assert
        assertTrue("La carga demo debe insertar cuando no hay datos", insertados)
        assertEquals(4, dao.registros.value.size)
        assertTrue(dao.registros.value.all { it.usuarioAlias == "ana" })
        assertTrue(dao.registros.value.all { !it.sincronizado })
    }

    @Test
    fun cargarDemoNoDuplicaDatosSiYaHayRegistros() = runBlocking {
        // Arrange
        val existente = registro(id = 7, tipo = "Existente")
        val dao = FakeRegistroDao(listOf(existente))
        val repository = RegistroRepository(dao)

        // Act
        val insertados = repository.cargarDemo("ana")

        // Assert
        assertFalse("La carga demo debe negarse si ya hay datos", insertados)
        assertEquals(1, dao.registros.value.size)
        assertEquals("Existente", dao.registros.value.single().tipo)
    }

    private fun registro(
        id: Int = 1,
        usuarioAlias: String = "ana",
        tipo: String = "Rutina",
        fechaHora: Long = 1_700_000_000_000L,
        sincronizado: Boolean = false
    ) = RegistroEntity(
        id = id,
        usuarioAlias = usuarioAlias,
        ambito = "ANIMO",
        tipo = tipo,
        intensidad = 3,
        contexto = "Casa",
        estrategia = "Respiración",
        nota = "Sin novedad",
        estadoObjetivo = "Mantener rutina",
        fechaHora = fechaHora,
        sincronizado = sincronizado
    )

    private class FakeRegistroDao(
        inicial: List<RegistroEntity> = emptyList()
    ) : RegistroDao {

        val registros = MutableStateFlow(inicial)

        override fun observarTodos(): Flow<List<RegistroEntity>> = registros

        override fun observarPorAmbito(ambito: String): Flow<List<RegistroEntity>> =
            registros.map { lista -> lista.filter { it.ambito == ambito } }

        override suspend fun obtenerPorId(id: Int): RegistroEntity? =
            registros.value.firstOrNull { it.id == id }

        override suspend fun obtenerTodos(): List<RegistroEntity> =
            registros.value.sortedWith(compareByDescending<RegistroEntity> { it.fechaHora }.thenByDescending { it.id })

        override suspend fun obtenerPendientes(): List<RegistroEntity> =
            registros.value.filter { !it.sincronizado }

        override suspend fun insertar(registro: RegistroEntity): Long {
            val id = if (registro.id == 0) {
                (registros.value.maxOfOrNull { it.id } ?: 0) + 1
            } else {
                registro.id
            }
            registros.value = registros.value + registro.copy(id = id)
            return id.toLong()
        }

        override suspend fun actualizar(registro: RegistroEntity): Int {
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

        override suspend fun contar(): Int = registros.value.size
    }
}
