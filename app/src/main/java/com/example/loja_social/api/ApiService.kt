package com.example.loja_social.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    // --- Rotas Públicas ---
    @GET("api/public/campanhas")
    suspend fun getCampanhas(): CampanhasResponse

    // --- Rota de Auth ---
    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse

    // --- ROTA DE ADMIN (PROTEGIDA) ---
    @GET("api/admin/beneficiarios")
    suspend fun getBeneficiarios(): BeneficiariosResponse

    // (Adicione aqui as outras rotas de admin quando precisar delas)
}