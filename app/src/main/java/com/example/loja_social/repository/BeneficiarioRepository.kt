package com.example.loja_social.repository

import com.example.loja_social.api.ApiService
import com.example.loja_social.api.BeneficiariosResponse
import com.example.loja_social.api.BeneficiarioRequest
import com.example.loja_social.api.FullSingleBeneficiarioResponse // <-- Import necessário para resolver o erro
import com.example.loja_social.api.SingleBeneficiarioResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BeneficiarioRepository(private val apiService: ApiService) {

    // Lista todos os beneficiários
    suspend fun getBeneficiarios(): BeneficiariosResponse {
        return withContext(Dispatchers.IO) {
            apiService.getBeneficiarios()
        }
    }

    /**
     * [NOVO] Obtém um único beneficiário (simulado usando a listagem completa, pois não há endpoint específico).
     */
    suspend fun getBeneficiario(beneficiarioId: String): FullSingleBeneficiarioResponse {
        return withContext(Dispatchers.IO) {
            // Primeiro, buscamos a lista completa (simulando a busca de um único item)
            val response = apiService.getBeneficiarios()

            if (response.success && response.data.isNotEmpty()) {
                // Filtramos a lista para encontrar o beneficiário com o ID
                val beneficiario = response.data.find { it.id == beneficiarioId }
                if (beneficiario != null) {
                    FullSingleBeneficiarioResponse(true, "Beneficiário carregado com sucesso.", beneficiario)
                } else {
                    FullSingleBeneficiarioResponse(false, "Beneficiário com ID '$beneficiarioId' não encontrado.", null)
                }
            } else {
                // Erro ao carregar lista (ou lista vazia)
                FullSingleBeneficiarioResponse(
                    response.success,
                    response.message ?: "Falha ao carregar lista de beneficiários da API.",
                    null
                )
            }
        }
    }

    // [NOVO] Cria um novo beneficiário (POST)
    suspend fun createBeneficiario(request: BeneficiarioRequest): SingleBeneficiarioResponse {
        return withContext(Dispatchers.IO) {
            apiService.createBeneficiario(request)
        }
    }

    // [NOVO] Atualiza um beneficiário existente (PUT)
    suspend fun updateBeneficiario(beneficiarioId: String, request: BeneficiarioRequest): SingleBeneficiarioResponse {
        return withContext(Dispatchers.IO) {
            apiService.updateBeneficiario(beneficiarioId, request)
        }
    }
}