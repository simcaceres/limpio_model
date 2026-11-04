package com.example.limpio_moviles.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "registros")
data class RegistroEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val usuarioAlias: String,
    val ambito: String,
    val tipo: String,
    val intensidad: Int,
    val contexto: String = "",
    val estrategia: String = "",
    val nota: String = "",
    val estadoObjetivo: String = "",
    val fechaHora: Long,
    val sincronizado: Boolean = false
)
