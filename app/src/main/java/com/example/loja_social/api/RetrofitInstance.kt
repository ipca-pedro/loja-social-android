package com.example.loja_social.api

import android.content.Context
import com.example.loja_social.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Transformado num Singleton que pode ser inicializado
 * para que possamos passar o 'Context' e usar o SessionManager.
 */
object RetrofitInstance {

    // URL da API
    private const val BASE_URL = "https://api-lojaipca.duckdns.org/"

    // O "espião" de logs
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Instância da API (agora 'lateinit var')
    lateinit var api: ApiService
        private set // Só pode ser definido dentro deste objeto

    /**
     * Esta função TEM de ser chamada na classe Application
     * ou na Activity principal antes de usar a API.
     */
    fun initialize(context: Context) {
        // Criar o SessionManager
        val sessionManager = SessionManager(context.applicationContext)

        // Criar o nosso "Injetor de Token"
        val authInterceptor = AuthInterceptor(sessionManager)

        // Criar o cliente OkHttp e adicionar AMBOS os interceptors
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor) // O "espião"
            .addInterceptor(authInterceptor)  // O "injetor de token"
            .build()

        // Construir o Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client) // Usar o nosso cliente configurado
            .build()

        // Criar a instância da API
        api = retrofit.create(ApiService::class.java)
    }
}