package com.example.loja_social.repository

import com.example.loja_social.api.AddStockRequest
import com.example.loja_social.api.AddStockResponse
import com.example.loja_social.api.ApiService
import com.example.loja_social.api.CategoriasResponse
import com.example.loja_social.api.DeleteStockResponse
import com.example.loja_social.api.ProdutosResponse
import com.example.loja_social.api.StockResponse
import com.example.loja_social.api.UpdateStockRequest
import com.example.loja_social.api.UpdateStockResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StockRepository(private val apiService: ApiService) {

    // RF3: Listar categorias para preencher o Spinner
    suspend fun getCategorias(): CategoriasResponse {
        return withContext(Dispatchers.IO) {
            apiService.getCategorias()
        }
    }

    // RF3: Listar produtos para preencher o Spinner
    suspend fun getProdutos(): ProdutosResponse {
        return withContext(Dispatchers.IO) {
            apiService.getProdutos()
        }
    }

    // RF3: Listar stock agrupado por produto
    suspend fun getStock(): StockResponse {
        return withContext(Dispatchers.IO) {
            apiService.getStock()
        }
    }

    // RF3: Adicionar novo lote de stock
    suspend fun addStock(request: AddStockRequest): AddStockResponse {
        return withContext(Dispatchers.IO) {
            apiService.addStock(request)
        }
    }

    // Atualizar lote de stock existente
    suspend fun updateStock(stockId: String, request: UpdateStockRequest): UpdateStockResponse {
        return withContext(Dispatchers.IO) {
            apiService.updateStock(stockId, request)
        }
    }

    // Remover lote de stock
    suspend fun deleteStock(stockId: String): DeleteStockResponse {
        return withContext(Dispatchers.IO) {
            apiService.deleteStock(stockId)
        }
    }
}