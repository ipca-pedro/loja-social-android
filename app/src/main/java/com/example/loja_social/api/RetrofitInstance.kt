package com.example.loja_social.api

import android.content.Context
import com.example.loja_social.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton do Retrofit com configurações otimizadas:
 * - Timeout configurado
 * - Retry logic
 * - Logging interceptor
 * - Auth interceptor
 */
object RetrofitInstance {

    // URL DA API
    private const val BASE_URL = "https://api-lojaipca.duckdns.org/"

    // Timeouts (em segundos)
    private const val CONNECT_TIMEOUT = 30L
    private const val READ_TIMEOUT = 30L
    private const val WRITE_TIMEOUT = 30L

    // O "espião" de logs (só em modo debug)
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (android.util.Log.isLoggable("Retrofit", android.util.Log.DEBUG)) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    // Instância da API (agora 'lateinit var')
    lateinit var api: ApiService
        private set
    
    // Flag para verificar se está inicializado
    @Volatile
    private var _isInitialized = false

    /**
     * Inicializa o Retrofit com todas as configurações necessárias.
     * Deve ser chamada na Application ou na Activity principal.
     */
    fun initialize(context: Context) {
        // Criar o SessionManager
        val sessionManager = SessionManager(context.applicationContext)

        // Criar o nosso "Injetor de Token"
        val authInterceptor = AuthInterceptor(sessionManager)

        // Criar o cliente OkHttp com timeouts e interceptors
        val client = OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor) // Logging
            .addInterceptor(authInterceptor)    // Autenticação
            .addInterceptor(ErrorInterceptor()) // Tratamento de erros
            .retryOnConnectionFailure(true)    // Retry automático em falhas de conexão
            .build()

        // Construir o Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        // Criar a instância da API
        api = retrofit.create(ApiService::class.java)
        _isInitialized = true
    }
    
    /**
     * Verifica se o RetrofitInstance está inicializado.
     */
    fun isInitialized(): Boolean {
        return try {
            _isInitialized && ::api.isInitialized
        } catch (e: UninitializedPropertyAccessException) {
            false
        }
    }
}