package com.example.loja_social.api

import retrofit2.http.GET

interface ApiService {

    @GET("api/public/campanhas")
    // MUDANÇA AQUI: De 'List<Campanha>' para 'CampanhasResponse'
    suspend fun getCampanhas(): CampanhasResponse
}