package com.example.limpio_moviles.data.repository

import android.content.Context
import android.content.SharedPreferences

class PerfilRepository(context: Context) {

    private val preferencias: SharedPreferences =
        context.applicationContext.getSharedPreferences(NOMBRE_PREFERENCIAS, Context.MODE_PRIVATE)

    fun cargarAlias(): String = preferencias.getString(CLAVE_ALIAS, "") ?: ""

    fun guardarAlias(alias: String) {
        preferencias.edit().putString(CLAVE_ALIAS, alias).apply()
    }

    fun cargarFotoPath(): String? = preferencias.getString(CLAVE_FOTO_PATH, null)

    fun guardarFotoPath(path: String?) {
        val editor = preferencias.edit()
        if (path == null) {
            editor.remove(CLAVE_FOTO_PATH)
        } else {
            editor.putString(CLAVE_FOTO_PATH, path)
        }
        editor.apply()
    }

    fun cargarRecordatorio(): Boolean = preferencias.getBoolean(CLAVE_RECORDATORIO, false)

    fun guardarRecordatorio(activo: Boolean) {
        preferencias.edit().putBoolean(CLAVE_RECORDATORIO, activo).apply()
    }

    private companion object {
        const val NOMBRE_PREFERENCIAS = "perfil_lemac"
        const val CLAVE_ALIAS = "alias"
        const val CLAVE_FOTO_PATH = "foto_path"
        const val CLAVE_RECORDATORIO = "recordatorio_activo"
    }
}
