package com.example.loja_social.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Interface do Retrofit que define todos os endpoints da API.
 */
interface ApiService {

    // --- Rotas Públicas ---
    @GET("api/public/campanhas")
    suspend fun getCampanhas(): CampanhasResponse

    // --- Rota de Auth ---
    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse

    // --- ROTAS DE ADMIN (PROTEGIDAS) ---

    // --- Gestão de Beneficiários (RF2) ---
    @GET("api/admin/beneficiarios")
    suspend fun getBeneficiarios(): BeneficiariosResponse

    @POST("api/admin/beneficiarios")
    suspend fun createBeneficiario(
        @Body request: BeneficiarioRequest
    ): SingleBeneficiarioResponse

    @PUT("api/admin/beneficiarios/{id}")
    suspend fun updateBeneficiario(
        @Path("id") beneficiarioId: String,
        @Body request: BeneficiarioRequest
    ): SingleBeneficiarioResponse

    // --- Gestão de Inventário (RF3 & RF6) ---
    @GET("api/admin/categorias")
    suspend fun getCategorias(): CategoriasResponse

    @GET("api/admin/produtos")
    suspend fun getProdutos(): ProdutosResponse

    @POST("api/admin/stock")
    suspend fun addStock(
        @Body request: AddStockRequest
    ): AddStockResponse

    @GET("api/admin/alertas/validade")
    suspend fun getAlertasValidade(): AlertasValidadeResponse

    // --- Gestão de Entregas (RF4) ---
    @GET("api/admin/entregas")
    suspend fun getEntregas(): EntregasResponse

    @POST("api/admin/entregas")
    suspend fun agendarEntrega(
        @Body request: AgendarEntregaRequest
    ): AgendarEntregaResponse

    @PUT("api/admin/entregas/{id}/concluir")
    suspend fun concluirEntrega(
        @Path("id") entregaId: String
    ): ConcluirEntregaResponse
}