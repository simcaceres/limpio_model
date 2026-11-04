package com.example.limpio_moviles.data.repository

import com.example.limpio_moviles.data.local.RegistroDao
import com.example.limpio_moviles.data.local.RegistroEntity
import com.example.limpio_moviles.data.remote.FakeLemacApi
import com.example.limpio_moviles.data.remote.LemacApi
import com.example.limpio_moviles.model.Ambito
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class RegistroRepository(
    private val dao: RegistroDao,
    private val api: LemacApi = FakeLemacApi()
) {

    val registros: Flow<List<RegistroEntity>> = dao.observarTodos()

    fun observarPorAmbito(ambito: Ambito): Flow<List<RegistroEntity>> =
        dao.observarPorAmbito(ambito.name)

    suspend fun guardar(registro: RegistroEntity) {
        if (registro.id == 0) {
            dao.insertar(registro)
        } else {
            dao.actualizar(registro)
        }
    }

    suspend fun eliminar(registro: RegistroEntity) {
        dao.eliminar(registro)
    }

    suspend fun obtener(id: Int): RegistroEntity? = dao.obtenerPorId(id)

    suspend fun eliminarTodos() {
        dao.eliminarTodos()
    }

    suspend fun sincronizar(): Int {
        val pendientes = dao.obtenerPendientes()
        if (pendientes.isEmpty()) return 0

        api.sincronizar(pendientes).getOrThrow()
        dao.marcarSincronizados(pendientes.map { it.id })
        return pendientes.size
    }

    suspend fun cargarDemo(alias: String) {
        if (dao.observarTodos().first().isNotEmpty()) return

        val ahora = System.currentTimeMillis()
        val registrosDemo = listOf(
            RegistroEntity(
                usuarioAlias = alias,
                ambito = Ambito.NEURODESARROLLO.name,
                tipo = "Rutina",
                intensidad = 3,
                contexto = "Mañana en casa",
                estrategia = "Lista de pasos",
                nota = "Rutina completada sin interrupciones",
                estadoObjetivo = "Mantener la constancia",
                fechaHora = ahora - UN_DIA_MILLIS,
                sincronizado = false
            ),
            RegistroEntity(
                usuarioAlias = alias,
                ambito = Ambito.DBT.name,
                tipo = "Mindfulness",
                intensidad = 4,
                contexto = "Ansiedad antes de una reunión",
                estrategia = "Respiración consciente",
                nota = "Tres minutos de atención plena",
                estadoObjetivo = "Practicar a diario",
                fechaHora = ahora - 2 * UN_DIA_MILLIS,
                sincronizado = false
            ),
            RegistroEntity(
                usuarioAlias = alias,
                ambito = Ambito.ANIMO.name,
                tipo = "Tranquilo",
                intensidad = 2,
                contexto = "Tarde de descanso",
                estrategia = "Caminata breve",
                nota = "Me sentí en calma",
                estadoObjetivo = "Registrar el ánimo",
                fechaHora = ahora - 3 * UN_DIA_MILLIS,
                sincronizado = false
            ),
            RegistroEntity(
                usuarioAlias = alias,
                ambito = Ambito.ADICCIONES.name,
                tipo = "Craving",
                intensidad = 6,
                contexto = "Después del trabajo",
                estrategia = "Llamar a una persona de apoyo",
                nota = "El impulso bajó tras la llamada",
                estadoObjetivo = "Sostener la abstinencia",
                fechaHora = ahora - 4 * UN_DIA_MILLIS,
                sincronizado = false
            )
        )

        registrosDemo.forEach { dao.insertar(it) }
    }

    private companion object {
        const val UN_DIA_MILLIS = 86_400_000L
    }
}
