package com.example.gestiondocumental.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    @Query("SELECT * FROM usuarios ORDER BY nombre")
    fun observarTodos(): Flow<List<UsuarioEntity>>

    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE rut = :rut LIMIT 1")
    suspend fun buscarPorRut(rut: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun porId(id: Long): UsuarioEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(usuario: UsuarioEntity)

    @Update
    suspend fun actualizar(usuario: UsuarioEntity)

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contar(): Int

    @Query("SELECT COALESCE(MAX(id), 0) FROM usuarios")
    suspend fun maxId(): Long
}

@Dao
interface DocumentoDao {
    @Query("SELECT * FROM documentos ORDER BY id DESC")
    fun observarTodos(): Flow<List<DocumentoEntity>>

    @Query("SELECT * FROM documentos WHERE id = :id LIMIT 1")
    suspend fun porId(id: Long): DocumentoEntity?

    @Insert
    suspend fun insertar(documento: DocumentoEntity): Long

    @Update
    suspend fun actualizar(documento: DocumentoEntity)

    @Query("SELECT COUNT(*) FROM documentos")
    suspend fun contar(): Int
}

@Dao
interface VersionDao {
    @Query("SELECT * FROM versiones WHERE documentoId = :documentoId ORDER BY numeroVersion DESC")
    fun observarPorDocumento(documentoId: Long): Flow<List<VersionEntity>>

    @Query("SELECT * FROM versiones WHERE documentoId = :documentoId ORDER BY numeroVersion DESC")
    suspend fun listarPorDocumento(documentoId: Long): List<VersionEntity>

    @Insert
    suspend fun insertar(version: VersionEntity)
}

@Dao
interface SolicitudDao {
    @Query("SELECT * FROM solicitudes ORDER BY id DESC")
    fun observarTodas(): Flow<List<SolicitudEntity>>

    @Query("SELECT * FROM solicitudes WHERE estado = 'PENDIENTE' ORDER BY id DESC")
    fun observarPendientes(): Flow<List<SolicitudEntity>>

    @Query("SELECT * FROM solicitudes WHERE id = :id LIMIT 1")
    suspend fun porId(id: Long): SolicitudEntity?

    @Insert
    suspend fun insertar(solicitud: SolicitudEntity): Long

    @Update
    suspend fun actualizar(solicitud: SolicitudEntity)

    @Query("UPDATE solicitudes SET estado = :estado, revisor = :revisor, motivoRechazo = :motivo, observacion = :observacion WHERE id = :id AND estado = 'PENDIENTE'")
    suspend fun resolver(id: Long, estado: String, revisor: String, motivo: String?, observacion: String?): Int
}

@Dao
interface AuditoriaDao {
    @Query("SELECT * FROM auditoria ORDER BY id DESC")
    fun observarTodas(): Flow<List<AuditoriaEntity>>

    @Insert
    suspend fun insertar(registro: AuditoriaEntity)
}

@Dao
interface DependenciaDao {
    @Query("SELECT * FROM dependencias ORDER BY nombre")
    fun observarTodas(): Flow<List<DependenciaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(dependencia: DependenciaEntity)
}
