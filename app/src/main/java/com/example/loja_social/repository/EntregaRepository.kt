package com.example.loja_social.repository

import com.example.loja_social.api.ApiResponse
import com.example.loja_social.api.ApiService
import com.example.loja_social.api.ConcluirEntregaResponse
import com.example.loja_social.api.EntregaDetailItem
import com.example.loja_social.api.EntregasResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository para operações relacionadas com entregas.
 * Centraliza chamadas à API para listar e concluir entregas.
 */
class EntregaRepository(private val apiService: ApiService) {

    /**
     * Obtém a lista de todas as entregas (agendadas e concluídas).
     * @return Resposta da API com lista de entregas
     */
    suspend fun getEntregas(): EntregasResponse {
        return withContext(Dispatchers.IO) {
            apiService.getEntregas()
        }
    }

    /**
     * Obtém os detalhes (itens) de uma entrega específica.
     */
    suspend fun getEntregaDetails(entregaId: String): ApiResponse<List<EntregaDetailItem>> {
        return withContext(Dispatchers.IO) {
            apiService.getEntregaDetails(entregaId)
        }
    }

    /**
     * Marca uma entrega agendada como concluída.
     * O trigger na base de dados faz automaticamente o abate do stock.
     * @param entregaId ID da entrega a concluir
     * @return Resposta da API com dados da entrega concluída
     */
    suspend fun concluirEntrega(entregaId: String): ConcluirEntregaResponse {
        return withContext(Dispatchers.IO) {
            // O Body é vazio no PUT /concluir, só precisa do ID no Path
            apiService.concluirEntrega(entregaId)
        }
    }
}