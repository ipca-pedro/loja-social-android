package com.example.loja_social.api

import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.POST
interface ApiService {

    @GET("api/public/campanhas")
    // MUDANÇA AQUI: De 'List<Campanha>' para 'CampanhasResponse'
    suspend fun getCampanhas(): CampanhasResponse

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse
}