package com.example.gestiondocumental.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas instrumentadas de la siembra convergente de datos de demostración
 * sobre una base Room real en memoria.
 *
 * Valida la siembra completa, la idempotencia, la reparación de estados
 * parciales heredados, el uso de ids reales de documentos, la reversión
 * transaccional ante fallos y la integridad referencial de las versiones.
 */
@RunWith(AndroidJUnit4::class)
class SembradorInstrumentedTest {

    private lateinit var db: GestionDatabase

    @Before
    fun crearBaseDeDatosEnMemoria() {
        val contexto = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(contexto, GestionDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun cerrarBaseDeDatos() {
        db.close()
    }

    @Test
    fun unaBaseVaciaSeSiembraConLosConteosEsperadosYAuditoriaDeSeed() = runBlocking {
        // Act
        Sembrador.sembrarSiVacio(db)

        // Assert
        assertEquals(7, contar("usuarios"))
        assertEquals(6, contar("documentos"))
        assertEquals(4, contar("dependencias"))
        assertEquals(7, contar("versiones"))
        assertEquals(1, contar("solicitudes"))
        assertEquals(1, contar("auditoria", "accion = 'SEED'"))
    }

    @Test
    fun sembrarDosVecesMantieneLosMismosConteos() = runBlocking {
        // Arrange
        Sembrador.sembrarSiVacio(db)

        // Act
        Sembrador.sembrarSiVacio(db)

        // Assert
        assertEquals(7, contar("usuarios"))
        assertEquals(6, contar("documentos"))
        assertEquals(4, contar("dependencias"))
        assertEquals(7, contar("versiones"))
        assertEquals(1, contar("solicitudes"))
        assertEquals(1, contar("auditoria", "accion = 'SEED'"))
    }

    @Test
    fun reparaUnaSiembraParcialHeredadaHastaLosConteosCompletos() = runBlocking {
        // Arrange: sólo existe Ana; faltan el resto de usuarios, documentos y versiones.
        db.usuarioDao().insertar(USUARIO_ANA)
        assertEquals(1, contar("usuarios"))

        // Act
        Sembrador.sembrarSiVacio(db)

        // Assert: convergencia a 7 usuarios sin duplicar el usuario preexistente.
        assertEquals(7, contar("usuarios"))
        assertEquals(1, contar("usuarios", "correo = 'ana.perez@salud.cl'"))
        assertEquals(7, escalar("SELECT COUNT(DISTINCT correo) FROM usuarios"))
        assertEquals(6, contar("documentos"))
        assertEquals(7, contar("versiones"))
        assertEquals(4, contar("dependencias"))
        assertEquals(1, contar("solicitudes"))
    }

    @Test
    fun sieteUsuariosPrecargadosYSinDocumentosSiembraSinDuplicarUsuarios() = runBlocking {
        // Arrange: escenario realista de una siembra parcial: usuarios completos, sin documentos.
        USUARIOS_DEMO.forEach { db.usuarioDao().insertar(it) }
        assertEquals(7, contar("usuarios"))
        assertEquals(0, contar("documentos"))

        // Act
        Sembrador.sembrarSiVacio(db)

        // Assert
        assertEquals(7, contar("usuarios"))
        assertEquals(7, escalar("SELECT COUNT(DISTINCT correo) FROM usuarios"))
        assertEquals(6, contar("documentos"))
        assertEquals(7, contar("versiones"))
        assertEquals(1, contar("auditoria", "accion = 'SEED'"))
    }

    @Test
    fun tresDeSieteUsuariosSeCompletanASieteExactosYSinDuplicados() = runBlocking {
        // Arrange
        USUARIOS_DEMO.take(3).forEach { db.usuarioDao().insertar(it) }
        assertEquals(3, contar("usuarios"))

        // Act
        Sembrador.sembrarSiVacio(db)

        // Assert
        assertEquals(7, contar("usuarios"))
        assertEquals(7, escalar("SELECT COUNT(DISTINCT correo) FROM usuarios"))
        assertEquals(7, escalar("SELECT COUNT(DISTINCT rut) FROM usuarios"))
        assertEquals(USUARIOS_DEMO.map { it.correo }.toSet(), correosUsuarios().toSet())
        assertEquals(6, contar("documentos"))
        assertEquals(7, contar("versiones"))
    }

    @Test
    fun lasVersionesApuntanALosIdsRealesAunqueSqliteSequenceEsteAdelantado() = runBlocking {
        // Arrange: insertar y borrar un documento para avanzar sqlite_sequence de documentos.
        val idDummy = db.documentoDao().insertar(DOCUMENTO_DUMMY)
        assertTrue("El documento dummy debió insertarse con id positivo", idDummy > 0)
        val writable = db.openHelper.writableDatabase
        writable.execSQL("DELETE FROM documentos WHERE id = $idDummy")
        writable.execSQL("UPDATE sqlite_sequence SET seq = 6 WHERE name = 'documentos'")
        if (escalar("SELECT COUNT(*) FROM sqlite_sequence WHERE name = 'documentos'") == 0) {
            writable.execSQL("INSERT OR REPLACE INTO sqlite_sequence (name, seq) VALUES ('documentos', 6)")
        }
        assertEquals(6, escalar("SELECT seq FROM sqlite_sequence WHERE name = 'documentos'"))

        // Act
        Sembrador.sembrarSiVacio(db)

        // Assert: los documentos nuevos son 7..12 y ninguna versión apunta a 1..6.
        assertEquals(6, contar("documentos"))
        assertEquals(7, contar("versiones"))
        assertEquals(7, escalar("SELECT MIN(documentoId) FROM versiones"))
        assertEquals(12, escalar("SELECT MAX(documentoId) FROM versiones"))
        assertEquals(
            7,
            escalar("SELECT COUNT(*) FROM versiones WHERE documentoId BETWEEN 7 AND 12")
        )
        assertEquals(
            0,
            escalar("SELECT COUNT(*) FROM versiones WHERE documentoId BETWEEN 1 AND 6")
        )
        assertEquals(
            0,
            escalar(
                "SELECT COUNT(*) FROM versiones v " +
                    "LEFT JOIN documentos d ON d.id = v.documentoId WHERE d.id IS NULL"
            )
        )
    }

    @Test
    fun unFalloAlInsertarVersionesRevierteTodaLaSiembra() = runBlocking {
        // Arrange: trigger que aborta la inserción de versiones a mitad de la siembra.
        val writable = db.openHelper.writableDatabase
        writable.execSQL(
            "CREATE TRIGGER fallo_simulado_versiones BEFORE INSERT ON versiones " +
                "BEGIN SELECT RAISE(ABORT, 'fallo simulado'); END"
        )
        try {
            // Act
            var excepcion: Exception? = null
            try {
                Sembrador.sembrarSiVacio(db)
            } catch (e: Exception) {
                excepcion = e
            }

            // Assert: la excepción se propaga y la transacción no deja rastro.
            assertNotNull("La siembra debió fallar por el trigger", excepcion)
            assertEquals(0, contar("usuarios"))
            assertEquals(0, contar("documentos"))
            assertEquals(0, contar("dependencias"))
            assertEquals(0, contar("versiones"))
            assertEquals(0, contar("solicitudes"))
            assertEquals(0, contar("auditoria"))
        } finally {
            writable.execSQL("DROP TRIGGER IF EXISTS fallo_simulado_versiones")
        }
    }

    @Test
    fun lasVersionesApuntanADocumentosExistentes() = runBlocking {
        // Act
        Sembrador.sembrarSiVacio(db)

        // Assert
        assertEquals(6, escalar("SELECT COUNT(DISTINCT documentoId) FROM versiones"))
        assertEquals(
            0,
            escalar(
                "SELECT COUNT(*) FROM versiones v " +
                    "LEFT JOIN documentos d ON d.id = v.documentoId WHERE d.id IS NULL"
            )
        )
        assertEquals(
            2,
            escalar(
                "SELECT COUNT(*) FROM versiones v " +
                    "JOIN documentos d ON d.id = v.documentoId " +
                    "WHERE d.titulo = 'Certificado de antigüedad'"
            )
        )
    }

    private fun contar(tabla: String, condicion: String? = null): Int =
        escalar("SELECT COUNT(*) FROM $tabla" + (condicion?.let { " WHERE $it" } ?: ""))

    private fun escalar(sql: String): Int =
        db.openHelper.readableDatabase.query(sql).use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }

    private fun correosUsuarios(): List<String> =
        db.openHelper.readableDatabase.query("SELECT correo FROM usuarios ORDER BY id").use { cursor ->
            buildList {
                while (cursor.moveToNext()) add(cursor.getString(0))
            }
        }

    private companion object {
        fun usuarioDemo(
            id: Long,
            rut: String,
            nombre: String,
            correo: String,
            rol: String,
            dependencia: String
        ) = UsuarioEntity(
            id = id,
            rut = rut,
            nombre = nombre,
            correo = correo,
            rol = rol,
            dependencia = dependencia,
            tipoContrato = "INDEFINIDO",
            activo = true,
            clave = "1234"
        )

        val USUARIO_ANA = usuarioDemo(
            1, "11111111-1", "Ana Pérez", "ana.perez@salud.cl", "FUNCIONARIO", "Atención Primaria"
        )

        val USUARIOS_DEMO = listOf(
            USUARIO_ANA,
            usuarioDemo(2, "22222222-2", "Bruno Díaz", "bruno.diaz@salud.cl", "JEFATURA", "Atención Primaria"),
            usuarioDemo(3, "33333333-3", "Carla Soto", "carla.soto@salud.cl", "ADMINISTRATIVO", "Recursos Humanos"),
            usuarioDemo(4, "44444444-4", "Diego Rojas", "diego.rojas@salud.cl", "RRHH", "Recursos Humanos"),
            usuarioDemo(5, "55555555-5", "Elena Muñoz", "elena.munoz@salud.cl", "ADMIN_SISTEMA", "Dirección de Salud"),
            usuarioDemo(6, "66666666-6", "Felipe Vega", "felipe.vega@salud.cl", "TI_CIBERSEGURIDAD", "Dirección de Salud"),
            usuarioDemo(7, "77777777-7", "Gabriela Paz", "gabriela.paz@salud.cl", "SUPERVISOR", "Hospital Base")
        )

        val DOCUMENTO_DUMMY = DocumentoEntity(
            usuarioId = 1,
            propietario = "Ana Pérez",
            categoria = "CONTRATO",
            titulo = "Documento dummy temporal",
            estado = "VIGENTE",
            versionActual = 1,
            fechaVencimiento = null,
            dependencia = "Atención Primaria"
        )
    }
}
