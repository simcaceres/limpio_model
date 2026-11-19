package com.example.gestiondocumental.data.repository

import com.example.gestiondocumental.data.local.AuditoriaDao
import com.example.gestiondocumental.data.local.AuditoriaEntity
import com.example.gestiondocumental.data.local.UsuarioDao
import com.example.gestiondocumental.data.local.UsuarioEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/**
 * Pruebas unitarias JVM de [SesionRepository] usando DAOs falsos en memoria.
 *
 * Se cubre el camino feliz, las validaciones de credenciales, la tolerancia a
 * fallos inesperados del DAO de usuarios, la propagación de cancelaciones y la
 * naturaleza best-effort de la auditoría ante errores ordinarios.
 */
class SesionRepositoryTest {

    @Test
    fun loginCorrectoDevuelveSuccessSeteaActualYRegistraLogin() = runTest {
        // Arrange
        val usuario = usuarioActivo()
        val usuarioDao = FakeUsuarioDao(entidad = usuario)
        val auditoriaDao = FakeAuditoriaDao()
        val repositorio = SesionRepository(usuarioDao, auditoriaDao)

        // Act
        val resultado = repositorio.iniciarSesion("  Ana.Perez@Salud.CL  ", "1234")

        // Assert
        assertTrue("Se esperaba success, fue: $resultado", resultado.isSuccess)
        assertEquals("ana.perez@salud.cl", resultado.getOrThrow().correo)
        assertEquals("ana.perez@salud.cl", repositorio.actual.value?.correo)
        assertEquals("ana.perez@salud.cl", usuarioDao.ultimoCorreoBuscado)
        assertEquals(listOf("LOGIN"), auditoriaDao.acciones())
        assertEquals(listOf("OK"), auditoriaDao.registros.map { it.resultado })
        assertEquals(listOf("Ana Pérez"), auditoriaDao.registros.map { it.usuario })
    }

    @Test
    fun correoInexistenteDevuelveFailureConMensajeYRegistraLoginFallido() = runTest {
        // Arrange
        val usuarioDao = FakeUsuarioDao(entidad = null)
        val auditoriaDao = FakeAuditoriaDao()
        val repositorio = SesionRepository(usuarioDao, auditoriaDao)

        // Act
        val resultado = repositorio.iniciarSesion("nadie@salud.cl", "1234")

        // Assert
        assertTrue("Se esperaba failure, fue: $resultado", resultado.isFailure)
        assertEquals(
            "No existe una cuenta con ese correo.",
            resultado.exceptionOrNull()?.message
        )
        assertNull(repositorio.actual.value)
        assertEquals(listOf("LOGIN_FALLIDO"), auditoriaDao.acciones())
        assertEquals(listOf("ERROR"), auditoriaDao.registros.map { it.resultado })
    }

    @Test
    fun cuentaInactivaDevuelveFailureConMensajeDeDesactivada() = runTest {
        // Arrange
        val usuarioDao = FakeUsuarioDao(entidad = usuarioActivo().copy(activo = false))
        val auditoriaDao = FakeAuditoriaDao()
        val repositorio = SesionRepository(usuarioDao, auditoriaDao)

        // Act
        val resultado = repositorio.iniciarSesion("ana.perez@salud.cl", "1234")

        // Assert
        assertTrue("Se esperaba failure, fue: $resultado", resultado.isFailure)
        assertEquals(
            "La cuenta está desactivada. Contacta a RR.HH.",
            resultado.exceptionOrNull()?.message
        )
        assertNull(repositorio.actual.value)
        assertEquals(listOf("LOGIN_FALLIDO"), auditoriaDao.acciones())
    }

    @Test
    fun claveIncorrectaDevuelveFailureConMensajeYRegistraLoginFallido() = runTest {
        // Arrange
        val usuarioDao = FakeUsuarioDao(entidad = usuarioActivo())
        val auditoriaDao = FakeAuditoriaDao()
        val repositorio = SesionRepository(usuarioDao, auditoriaDao)

        // Act
        val resultado = repositorio.iniciarSesion("ana.perez@salud.cl", "mala")

        // Assert
        assertTrue("Se esperaba failure, fue: $resultado", resultado.isFailure)
        assertEquals("Clave incorrecta.", resultado.exceptionOrNull()?.message)
        assertNull(repositorio.actual.value)
        assertEquals(listOf("LOGIN_FALLIDO"), auditoriaDao.acciones())
        assertEquals(listOf("ERROR"), auditoriaDao.registros.map { it.resultado })
    }

    @Test
    fun siLaAuditoriaLanzaElLoginSigueSiendoSuccessYActualQuedaSeteado() = runTest {
        // Arrange
        val usuarioDao = FakeUsuarioDao(entidad = usuarioActivo())
        val auditoriaDao = FakeAuditoriaDao().apply { fallarAlInsertar = true }
        val repositorio = SesionRepository(usuarioDao, auditoriaDao)

        // Act
        val resultado = repositorio.iniciarSesion("ana.perez@salud.cl", "1234")

        // Assert
        assertTrue("La auditoría no debe romper el login, fue: $resultado", resultado.isSuccess)
        assertEquals("ana.perez@salud.cl", repositorio.actual.value?.correo)
        assertEquals(1, auditoriaDao.intentosDeInsercion)
    }

    @Test
    fun siElDaoDeUsuariosLanzaDevuelveFailureDeAccesoALosDatos() = runTest {
        // Arrange
        val usuarioDao = FakeUsuarioDao(entidad = null).apply {
            excepcionAlBuscar = IllegalStateException("base de datos bloqueada")
        }
        val auditoriaDao = FakeAuditoriaDao()
        val repositorio = SesionRepository(usuarioDao, auditoriaDao)

        // Act
        val resultado = repositorio.iniciarSesion("ana.perez@salud.cl", "1234")

        // Assert
        assertTrue("Se esperaba failure, fue: $resultado", resultado.isFailure)
        assertEquals(
            "No se pudo acceder a los datos locales. Intenta nuevamente.",
            resultado.exceptionOrNull()?.message
        )
        assertNull(repositorio.actual.value)
        assertTrue("No debe auditar cuando falla el acceso", auditoriaDao.registros.isEmpty())
    }

    @Test
    fun siElDaoDeUsuariosLanzaCancellationExceptionLaPropagaSinDevolverFailure() = runTest {
        // Arrange
        val cancelacion = CancellationException("cancelación de prueba")
        val usuarioDao = FakeUsuarioDao(entidad = null).apply { excepcionAlBuscar = cancelacion }
        val auditoriaDao = FakeAuditoriaDao()
        val repositorio = SesionRepository(usuarioDao, auditoriaDao)

        // Act + Assert
        try {
            repositorio.iniciarSesion("ana.perez@salud.cl", "1234")
            fail("Se esperaba CancellationException, pero iniciarSesion retornó normalmente")
        } catch (e: CancellationException) {
            assertSame(cancelacion, e)
        }
        assertNull(repositorio.actual.value)
        assertEquals("No debe auditar cuando se cancela el acceso", 0, auditoriaDao.intentosDeInsercion)
    }

    @Test
    fun siLaAuditoriaDelLoginLanzaCancellationExceptionSePropagaSinExitoSilencioso() = runTest {
        // Arrange
        val cancelacion = CancellationException("auditoría cancelada")
        val auditoriaDao = FakeAuditoriaDao().apply { excepcionAlInsertar = cancelacion }
        val repositorio = SesionRepository(FakeUsuarioDao(entidad = usuarioActivo()), auditoriaDao)

        // Act + Assert
        try {
            val resultado = repositorio.iniciarSesion("ana.perez@salud.cl", "1234")
            fail("Se esperaba CancellationException, pero se devolvió $resultado")
        } catch (e: CancellationException) {
            assertSame(cancelacion, e)
        }
        assertNull("La sesión no debe quedar iniciada si se cancela la auditoría", repositorio.actual.value)
        assertEquals(1, auditoriaDao.intentosDeInsercion)
        assertTrue("La auditoría cancelada no debe quedar registrada", auditoriaDao.registros.isEmpty())
    }

    @Test
    fun cerrarSesionConAuditoriaCanceladaPropagaYDejaActualEnNull() = runTest {
        // Arrange
        val cancelacion = CancellationException("auditoría cancelada al cerrar")
        val auditoriaDao = FakeAuditoriaDao()
        val repositorio = SesionRepository(FakeUsuarioDao(usuarioActivo()), auditoriaDao)
        repositorio.iniciarSesion("ana.perez@salud.cl", "1234")
        auditoriaDao.excepcionAlInsertar = cancelacion

        // Act + Assert
        try {
            repositorio.cerrarSesion()
            fail("Se esperaba CancellationException, pero cerrarSesion retornó normalmente")
        } catch (e: CancellationException) {
            assertSame(cancelacion, e)
        }
        assertNull("La sesión debe limpiarse aunque se cancele la auditoría", repositorio.actual.value)
        assertEquals(2, auditoriaDao.intentosDeInsercion)
        assertEquals(listOf("LOGIN"), auditoriaDao.acciones())
    }

    @Test
    fun cerrarSesionExitosaRegistraLogoutYLimpiaActual() = runTest {
        // Arrange
        val auditoriaDao = FakeAuditoriaDao()
        val repositorio = SesionRepository(FakeUsuarioDao(usuarioActivo()), auditoriaDao)
        repositorio.iniciarSesion("ana.perez@salud.cl", "1234")

        // Act
        repositorio.cerrarSesion()

        // Assert
        assertNull(repositorio.actual.value)
        assertEquals(listOf("LOGIN", "LOGOUT"), auditoriaDao.acciones())
        assertEquals(listOf("OK", "OK"), auditoriaDao.registros.map { it.resultado })
        assertEquals("Cierre de sesión", auditoriaDao.registros.last().detalle)
    }

    private fun usuarioActivo() = UsuarioEntity(
        id = 1,
        rut = "11111111-1",
        nombre = "Ana Pérez",
        correo = "ana.perez@salud.cl",
        rol = "FUNCIONARIO",
        dependencia = "Atención Primaria",
        tipoContrato = "INDEFINIDO",
        activo = true,
        clave = "1234"
    )

    /** DAO falso: sólo implementa la búsqueda usada por el repositorio. */
    private class FakeUsuarioDao(entidad: UsuarioEntity?) : UsuarioDao {

        var entidad: UsuarioEntity? = entidad
        var excepcionAlBuscar: Throwable? = null
        var ultimoCorreoBuscado: String? = null
            private set

        override suspend fun buscarPorCorreo(correo: String): UsuarioEntity? {
            ultimoCorreoBuscado = correo
            excepcionAlBuscar?.let { throw it }
            return entidad
        }

        override fun observarTodos(): Flow<List<UsuarioEntity>> = emptyFlow()

        override suspend fun buscarPorRut(rut: String): UsuarioEntity? =
            throw UnsupportedOperationException("No usado en SesionRepositoryTest")

        override suspend fun porId(id: Long): UsuarioEntity? =
            throw UnsupportedOperationException("No usado en SesionRepositoryTest")

        override suspend fun insertar(usuario: UsuarioEntity) =
            throw UnsupportedOperationException("No usado en SesionRepositoryTest")

        override suspend fun actualizar(usuario: UsuarioEntity) =
            throw UnsupportedOperationException("No usado en SesionRepositoryTest")

        override suspend fun contar(): Int =
            throw UnsupportedOperationException("No usado en SesionRepositoryTest")

        override suspend fun maxId(): Long =
            throw UnsupportedOperationException("No usado en SesionRepositoryTest")
    }

    /** DAO falso que acumula auditoría y permite simular fallos de inserción. */
    private class FakeAuditoriaDao : AuditoriaDao {

        val registros = mutableListOf<AuditoriaEntity>()
        var fallarAlInsertar: Boolean = false
        var excepcionAlInsertar: Throwable? = null
        var intentosDeInsercion: Int = 0
            private set

        fun acciones(): List<String> = registros.map { it.accion }

        override suspend fun insertar(registro: AuditoriaEntity) {
            intentosDeInsercion++
            excepcionAlInsertar?.let { throw it }
            if (fallarAlInsertar) {
                throw IllegalStateException("auditoría no disponible")
            }
            registros += registro
        }

        override fun observarTodas(): Flow<List<AuditoriaEntity>> = emptyFlow()
    }
}
