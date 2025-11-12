package com.example.loja_social.repository

import com.example.loja_social.api.AddStockRequest
import com.example.loja_social.api.AddStockResponse
import com.example.loja_social.api.ApiService
import com.example.loja_social.api.CategoriasResponse
import com.example.loja_social.api.ProdutosResponse
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

    // RF3: Adicionar novo lote de stock
    suspend fun addStock(request: AddStockRequest): AddStockResponse {
        return withContext(Dispatchers.IO) {
            apiService.addStock(request)
        }
    }
}