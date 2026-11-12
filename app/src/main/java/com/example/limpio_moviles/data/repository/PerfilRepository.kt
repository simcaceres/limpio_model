package com.example.limpio_moviles.data.repository

interface PerfilRepository {

    fun cargarAlias(): String

    fun guardarAlias(alias: String)

    fun cargarFotoPath(): String?

    fun guardarFotoPath(path: String?)

    fun cargarRecordatorio(): Boolean

    fun guardarRecordatorio(activo: Boolean)
}
