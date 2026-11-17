package com.example.gestiondocumental.data.repository

import com.example.gestiondocumental.data.remote.DocumentosApiService
import com.example.gestiondocumental.data.remote.RedDocumentos
import com.example.gestiondocumental.data.remote.aModelo
import com.example.gestiondocumental.model.Documento
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repositorio de documentos remotos sobre la API REST simulada.
 *
 * Traduce los DTO de red al modelo de dominio y ordena el resultado por
 * identificador ascendente. Los errores de red o de formato se devuelven
 * como [Result.failure] para que la capa de presentacion los muestre.
 */
class DocumentosRemotosRepository(
    private val api: DocumentosApiService = RedDocumentos.api,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun obtenerDocumentos(): Result<List<Documento>> = withContext(dispatcher) {
        try {
            Result.success(
                api.obtenerDocumentos()
                    .map { it.aModelo() }
                    .sortedBy { it.id }
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
