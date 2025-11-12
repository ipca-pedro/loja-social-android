package com.example.loja_social.repository

import com.example.loja_social.api.ApiService
import com.example.loja_social.api.ConcluirEntregaResponse
import com.example.loja_social.api.EntregasResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EntregaRepository(private val apiService: ApiService) {

    suspend fun getEntregas(): EntregasResponse {
        return withContext(Dispatchers.IO) {
            apiService.getEntregas()
        }
    }

    /**
     * RF4: Marca uma entrega como concluída, o trigger na BD faz o abate do stock.
     */
    suspend fun concluirEntrega(entregaId: String): ConcluirEntregaResponse {
        return withContext(Dispatchers.IO) {
            // O Body é vazio no PUT /concluir, só precisa do ID no Path
            apiService.concluirEntrega(entregaId)
        }
    }
}