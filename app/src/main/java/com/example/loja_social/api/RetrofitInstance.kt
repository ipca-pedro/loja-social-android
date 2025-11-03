package com.example.loja_social.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Este "object" (Singleton) constrói a instância do Retrofit.
 * É o "construtor" que liga tudo.
 */
object RetrofitInstance {

    // !!! URL API !!!
    private const val BASE_URL = "https://api-lojaipca.duckdns.org/"

    // 'lazy' significa que o objeto 'api' só será criado
    // na primeira vez que for usado.
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            // Diz ao Retrofit para usar o Gson para converter JSON em Data Classes
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java) // Cria a implementação do nosso 'ApiService'
    }
}