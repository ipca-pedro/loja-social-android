package com.example.loja_social.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

// --- DEFINIÇÃO DO MODELO BENEFICIÁRIO ---
// (Pode mover isto para o models.kt se preferir)
data class Beneficiario(
    @SerializedName("id")
    val id: String,
    @SerializedName("nome_completo")
    val nomeCompleto: String,
    @SerializedName("num_estudante")
    val numEstudante: String?,
    @SerializedName("email")
    val email: String?,
    @SerializedName("estado")
    val estado: String
)

// --- DEFINIÇÃO DO "ENVELOPE" DA RESPOSTA ---
data class BeneficiariosResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: List<Beneficiario>
)

// --- INTERFACE DA API ATUALIZADO ---
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
    // NOTA: Não precisamos de @Header("Authorization") aqui,
    // o Interceptor vai tratar disso automaticamente.
    @GET("api/admin/beneficiarios")
    suspend fun getBeneficiarios(): BeneficiariosResponse
}