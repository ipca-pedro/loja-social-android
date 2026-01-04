package com.example.loja_social.repository

import com.example.loja_social.api.AddStockRequest
import com.example.loja_social.api.AddStockResponse
import com.example.loja_social.api.ApiService
import com.example.loja_social.api.ApiResponse
import com.example.loja_social.api.CampanhasResponse
import com.example.loja_social.api.CategoriasResponse
import com.example.loja_social.api.LotesResponse
import com.example.loja_social.api.ProdutosResponse
import com.example.loja_social.api.StockResponse
import com.example.loja_social.api.UpdateStockRequest
import com.example.loja_social.api.UpdateStockResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository para operações relacionadas com stock.
 * Centraliza todas as chamadas à API de gestão de stock, categorias e produtos.
 */
class StockRepository(private val apiService: ApiService) {

    /**
     * Obtém a lista de todas as categorias disponíveis.
     * Usado para popular o dropdown de categorias no formulário de adicionar stock.
     * @return Resposta da API com a lista de categorias
     */
    suspend fun getCategorias(): CategoriasResponse {
        return withContext(Dispatchers.IO) {
            apiService.getCategorias()
        }
    }

    /**
     * Obtém a lista de todos os produtos disponíveis.
     * Usado para popular o dropdown de produtos no formulário de adicionar stock.
     * @return Resposta da API com a lista de produtos
     */
    suspend fun getProdutos(): ProdutosResponse {
        return withContext(Dispatchers.IO) {
            apiService.getProdutos()
        }
    }

    /**
     * Cria um novo produto.
     * @param request Dados do produto a criar
     * @return Resposta da API
     */
    suspend fun createProduct(request: com.example.loja_social.api.CreateProductRequest): com.example.loja_social.api.CreateProductResponse {
        return withContext(Dispatchers.IO) {
            apiService.createProduct(request)
        }
    }

    /**
     * Obtém a lista de todas as campanhas disponíveis.
     * @return Resposta da API com a lista de campanhas
     */
    suspend fun getCampanhas(): CampanhasResponse {
        return withContext(Dispatchers.IO) {
            apiService.getCampanhas()
        }
    }

    /**
     * Obtém o stock agrupado por produto.
     * Retorna uma lista com cada produto e a sua quantidade total e lotes.
     * @return Resposta da API com o stock agrupado
     */
    suspend fun getStock(): StockResponse {
        return withContext(Dispatchers.IO) {
            apiService.getStock()
        }
    }

    /**
     * Adiciona um novo lote de stock.
     * @param request Dados do novo lote (produto, categoria, quantidade, data de validade)
     * @return Resposta da API confirmando a criação
     */
    suspend fun addStock(request: AddStockRequest): AddStockResponse {
        return withContext(Dispatchers.IO) {
            apiService.addStock(request)
        }
    }

    /**
     * Atualiza um lote de stock existente.
     * Permite alterar a quantidade e a data de validade de um lote.
     * @param stockId O ID (UUID) do lote a atualizar
     * @param request Dados atualizados (quantidade e/ou data de validade)
     * @return Resposta da API confirmando a atualização
     */
    suspend fun updateStock(stockId: String, request: UpdateStockRequest): UpdateStockResponse {
        return withContext(Dispatchers.IO) {
            apiService.updateStock(stockId, request)
        }
    }

    suspend fun reportarDano(id: String): com.example.loja_social.api.ReportDamageResponse {
        return withContext(Dispatchers.IO) {
            apiService.reportarDano(id)
        }
    }

    /**
     * Remove um lote de stock.
     * @param loteId O ID do lote a remover
     * @return Resposta da API confirmando a remoção
     */
    suspend fun deleteLote(loteId: String): ApiResponse<Any> {
        return withContext(Dispatchers.IO) {
            apiService.deleteLote(loteId)
        }
    }

    /**
     * Obtém todos os lotes individuais de um produto específico.
     * Usado na tela de detalhes do stock para mostrar todos os lotes de um produto.
     * @param produtoId O ID do produto
     * @return Resposta da API com a lista de lotes do produto
     */
    suspend fun getLotesByProduto(produtoId: Int): LotesResponse {
        return withContext(Dispatchers.IO) {
            apiService.getLotesByProduto(produtoId)
        }
    }

    /**
     * Obtém todos os lotes disponíveis (com quantidade > 0).
     * Usado no formulário de agendamento de entregas para permitir seleção de lotes.
     * @return Resposta da API com todos os lotes disponíveis
     */
    suspend fun getAllLotes(): LotesResponse {
        return withContext(Dispatchers.IO) {
            apiService.getAllLotes()
        }
    }
}