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

    // Reutiliza o endpoint de beneficiários para o dropdown
    suspend fun getBeneficiarios(): BeneficiariosResponse {
        return withContext(Dispatchers.IO) {
            apiService.getBeneficiarios()
        }
    }

    // RF4: Agenda a entrega (a listagem de itens é temporariamente vazia para simplificação)
    suspend fun agendarEntrega(request: AgendarEntregaRequest): AgendarEntregaResponse {
        return withContext(Dispatchers.IO) {
            apiService.agendarEntrega(request)
        }
    }
}