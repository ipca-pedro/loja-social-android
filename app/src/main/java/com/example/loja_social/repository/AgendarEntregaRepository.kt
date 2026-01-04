package com.example.loja_social.repository

import com.example.loja_social.api.AgendarEntregaRequest
import com.example.loja_social.api.AgendarEntregaResponse
import com.example.loja_social.api.ApiService
import com.example.loja_social.api.BeneficiariosResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository focado em carregar dados para o formulário e submeter a entrega.
 */
class AgendarEntregaRepository(private val apiService: ApiService) {

    /**
     * Obtém a lista de todos os beneficiários disponíveis.
     * Usado para popular o dropdown de seleção no formulário de agendamento.
     * @return Resposta da API com a lista de beneficiários
     */
    suspend fun getBeneficiarios(): BeneficiariosResponse {
        return withContext(Dispatchers.IO) {
            apiService.getBeneficiarios()
        }
    }

    /**
     * Agenda uma nova entrega com os itens de stock selecionados.
     * @param request Requisição contendo beneficiário, data e lista de itens
     * @return Resposta da API com os dados da entrega criada
     */
    suspend fun agendarEntrega(request: AgendarEntregaRequest): AgendarEntregaResponse {
        return withContext(Dispatchers.IO) {
            apiService.agendarEntrega(request)
        }
    }

    suspend fun editarEntrega(id: String, request: AgendarEntregaRequest): AgendarEntregaResponse {
        return withContext(Dispatchers.IO) {
            apiService.editarEntrega(id, request)
        }
    }

    suspend fun getDetalhesEntrega(id: String): com.example.loja_social.api.ApiResponse<List<com.example.loja_social.api.EntregaDetailItem>> {
        return withContext(Dispatchers.IO) {
            apiService.getEntregaDetails(id)
        }
    }

    suspend fun getEntrega(id: String): com.example.loja_social.api.ApiResponse<com.example.loja_social.api.EntregaHeader> {
        return withContext(Dispatchers.IO) {
            apiService.getEntrega(id)
        }
    }
}