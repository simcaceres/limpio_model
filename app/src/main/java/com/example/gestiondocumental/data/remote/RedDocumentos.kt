package com.example.gestiondocumental.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Punto de acceso por defecto a la API de documentos.
 *
 * Los tests pueden construir su propio [Retrofit] contra un `MockWebServer`
 * sin depender de este objeto.
 */
object RedDocumentos {

    private const val TIMEOUT_SEGUNDOS = 10L

    private val cliente: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)
        .callTimeout(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)
        .build()

    val api: DocumentosApiService = Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL)
        .client(cliente)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(DocumentosApiService::class.java)
}
