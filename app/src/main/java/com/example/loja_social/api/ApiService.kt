package com.example.loja_social.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Interface do Retrofit que define todos os endpoints da API.
 * Todas as rotas retornam respostas padronizadas com {success, message, data}
 */
interface ApiService {

    // ===== ROTAS PÚBLICAS =====
    
    @GET("api/public/campanhas")
    suspend fun getCampanhas(): CampanhasResponse

    @GET("api/public/stock-summary")
    suspend fun getStockSummary(): StockSummaryResponse

    @POST("api/public/contacto")
    suspend fun enviarContacto(
        @Body request: ContactoRequest
    ): ContactoResponse

    // ===== AUTENTICAÇÃO =====
    
    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse

    // ===== ROTAS DE ADMIN (PROTEGIDAS) =====

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

    @GET("api/admin/stock")
    suspend fun getStock(): StockResponse

    @POST("api/admin/stock")
    suspend fun addStock(
        @Body request: AddStockRequest
    ): AddStockResponse

    @PUT("api/admin/stock/{id}")
    suspend fun updateStock(
        @Path("id") stockId: String,
        @Body request: UpdateStockRequest
    ): UpdateStockResponse

    @DELETE("api/admin/stock/{id}")
    suspend fun deleteStock(
        @Path("id") stockId: String
    ): DeleteStockResponse

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