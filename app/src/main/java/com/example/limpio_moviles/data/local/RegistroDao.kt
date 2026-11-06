package com.example.limpio_moviles.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RegistroDao {

    @Query("SELECT * FROM registros ORDER BY fechaHora DESC, id DESC")
    fun observarTodos(): Flow<List<RegistroEntity>>

    @Query("SELECT * FROM registros WHERE ambito = :ambito ORDER BY fechaHora DESC, id DESC")
    fun observarPorAmbito(ambito: String): Flow<List<RegistroEntity>>

    @Query("SELECT * FROM registros WHERE id = :id")
    suspend fun obtenerPorId(id: Int): RegistroEntity?

    @Query("SELECT * FROM registros ORDER BY fechaHora DESC, id DESC")
    suspend fun obtenerTodos(): List<RegistroEntity>

    @Query("SELECT * FROM registros WHERE sincronizado = 0")
    suspend fun obtenerPendientes(): List<RegistroEntity>

    @Insert
    suspend fun insertar(registro: RegistroEntity): Long

    @Update
    suspend fun actualizar(registro: RegistroEntity): Int

    @Delete
    suspend fun eliminar(registro: RegistroEntity)

    @Query("UPDATE registros SET sincronizado = 1 WHERE id IN (:ids)")
    suspend fun marcarSincronizados(ids: List<Int>)

    @Query("DELETE FROM registros")
    suspend fun eliminarTodos()

    @Query("SELECT COUNT(*) FROM registros")
    suspend fun contar(): Int

    @Transaction
    suspend fun insertarDemoSiVacio(registros: List<RegistroEntity>): Boolean {
        if (contar() > 0) return false
        registros.forEach { insertar(it) }
        return true
    }
}
