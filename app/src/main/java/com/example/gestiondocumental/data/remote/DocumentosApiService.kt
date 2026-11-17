package com.example.gestiondocumental.data.remote

import retrofit2.http.GET

/** Endpoints REST del modulo de gestion documental. */
interface DocumentosApiService {

    /** Lista los documentos publicados por la API simulada. */
    @GET(ApiConfig.ENDPOINT_DOCUMENTOS)
    suspend fun obtenerDocumentos(): List<DocumentoDto>
}
