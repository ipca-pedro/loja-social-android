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
    
    /**
     * Obtém a lista de campanhas ativas.
     */
    @GET("api/public/campanhas")
    suspend fun getCampanhas(): CampanhasResponse

    @POST("api/admin/campanhas")
    suspend fun createCampanha(@Body request: CampanhaRequest): SingleCampanhaResponse

    @PUT("api/admin/campanhas/{id}")
    suspend fun updateCampanha(@Path("id") id: String, @Body request: CampanhaRequest): SingleCampanhaResponse

    @DELETE("api/admin/campanhas/{id}")
    suspend fun deleteCampanha(@Path("id") id: String): ApiResponse<Any>

    /**
     * Obtém o resumo público de stock disponível.
     */
    @GET("api/public/stock-summary")
    suspend fun getStockSummary(): StockSummaryResponse

    /**
     * Envia uma mensagem de contacto.
     * @param request Dados do contacto (nome, email, mensagem)
     */
    @POST("api/public/contacto")
    suspend fun enviarContacto(
        @Body request: ContactoRequest
    ): ContactoResponse

    // ===== AUTENTICAÇÃO =====
    
    /**
     * Autentica um utilizador e obtém um token JWT.
     * @param request Credenciais de login (email e password)
     * @return Resposta com token JWT se o login for bem-sucedido
     */
    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse

    // ===== ROTAS DE ADMIN (PROTEGIDAS) =====

    // --- Gestão de Beneficiários (RF2) ---
    
    /**
     * Obtém a lista de todos os beneficiários.
     * Requer autenticação (rota protegida).
     */
    @GET("api/admin/beneficiarios")
    suspend fun getBeneficiarios(): BeneficiariosResponse

    /**
     * Cria um novo beneficiário.
     * @param request Dados do beneficiário a criar
     */
    @POST("api/admin/beneficiarios")
    suspend fun createBeneficiario(
        @Body request: BeneficiarioRequest
    ): SingleBeneficiarioResponse

    /**
     * Atualiza um beneficiário existente.
     * @param beneficiarioId ID do beneficiário a atualizar
     * @param request Dados atualizados do beneficiário
     */
    @PUT("api/admin/beneficiarios/{id}")
    suspend fun updateBeneficiario(
        @Path("id") beneficiarioId: String,
        @Body request: BeneficiarioRequest
    ): SingleBeneficiarioResponse

    // --- Gestão de Inventário (RF3 & RF6) ---
    
    /**
     * Obtém a lista de todas as categorias de produtos.
     */
    @GET("api/admin/categorias")
    suspend fun getCategorias(): CategoriasResponse

    /**
     * Obtém a lista de todos os produtos.
     */
    @GET("api/admin/produtos")
    suspend fun getProdutos(): ProdutosResponse

    /**
     * Cria um novo tipo de produto.
     * @param request Dados do produto a criar (nome, descrição, categoria)
     */
    @POST("api/admin/produtos")
    suspend fun createProduct(
        @Body request: CreateProductRequest
    ): CreateProductResponse

    /**
     * Obtém a lista de stock agregado por produto.
     */
    @GET("api/admin/stock")
    suspend fun getStock(): StockResponse

    /**
     * Obtém todos os lotes individuais de um produto específico.
     * @param produtoId ID do produto
     */
    @GET("api/admin/stock/produto/{produto_id}")
    suspend fun getLotesByProduto(
        @Path("produto_id") produtoId: Int
    ): LotesResponse

    /**
     * Obtém todos os lotes individuais de todos os produtos.
     */
    @GET("api/admin/stock/lotes")
    suspend fun getAllLotes(): LotesResponse

    /**
     * Adiciona novo stock (cria um novo lote).
     * @param request Dados do lote a criar (produto, quantidade, validade)
     */
    @POST("api/admin/stock")
    suspend fun addStock(
        @Body request: AddStockRequest
    ): AddStockResponse

    /**
     * Atualiza um lote de stock existente.
     * @param stockId ID do lote a atualizar
     * @param request Dados atualizados (quantidade atual, data de validade)
     */
    @PUT("api/admin/stock/{id}")
    suspend fun updateStock(
        @Path("id") stockId: String,
        @Body request: UpdateStockRequest
    ): UpdateStockResponse

    @POST("api/admin/stock/{id}/danificar")
    suspend fun reportarDano(
        @Path("id") id: String
    ): ReportDamageResponse

    /**
     * Remove um lote de stock.
     * @param id ID do lote a remover
     */
    @DELETE("api/stock/{id}")
    suspend fun deleteLote(@Path("id") id: String): ApiResponse<Any>

    /**
     * Obtém a lista de alertas de validade (produtos próximos do vencimento).
     */
    @GET("api/admin/alertas/validade")
    suspend fun getAlertasValidade(): AlertasValidadeResponse

    // --- Gestão de Entregas (RF4) ---
    
    /**
     * Obtém a lista de todas as entregas (agendadas e concluídas).
     */
    @GET("api/admin/entregas")
    suspend fun getEntregas(): EntregasResponse

    /**
     * Obtém os detalhes (itens) de uma entrega específica.
     */
    @GET("api/admin/entregas/{id}/detalhes")
    suspend fun getEntregaDetails(@Path("id") entregaId: String): ApiResponse<List<EntregaDetailItem>>

    /**
     * Agenda uma nova entrega.
     * @param request Dados da entrega (beneficiário, data, itens)
     */
    @POST("api/admin/entregas")
    suspend fun agendarEntrega(
        @Body request: AgendarEntregaRequest
    ): AgendarEntregaResponse

    /**
     * Conclui uma entrega agendada (marca como entregue).
     * @param entregaId ID da entrega a concluir
     */
    @PUT("api/admin/entregas/{id}/concluir")
    suspend fun concluirEntrega(
        @Path("id") entregaId: String
    ): ConcluirEntregaResponse

    /**
     * Cancela uma entrega agendada e remove a reserva de stock.
     */
    @DELETE("api/admin/entregas/{id}")
    suspend fun deleteEntrega(@Path("id") entregaId: String): ApiResponse<Any>

    // ===== ROTAS DE BENEFICIÁRIO (PROTEGIDAS) =====
    
    /**
     * Obtém as entregas do beneficiário autenticado.
     * Usa o endpoint de admin mas filtra pelo beneficiário logado.
     */
    @GET("api/admin/entregas")
    suspend fun getMinhasEntregas(): EntregasResponse

    // ===== RELATÓRIOS =====
    
    @GET("api/admin/relatorios/entregas")
    suspend fun getRelatorioEntregas(
        @retrofit2.http.Query("inicio") inicio: String?,
        @retrofit2.http.Query("fim") fim: String?
    ): RelatorioEntregasResponse

    @GET("api/admin/relatorios/stock")
    suspend fun getRelatorioStock(): RelatorioStockResponse

    @GET("api/admin/relatorios/validade")
    suspend fun getRelatorioValidade(): RelatorioValidadeResponse
}