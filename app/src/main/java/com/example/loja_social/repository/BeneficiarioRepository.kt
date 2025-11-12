package com.example.loja_social.repository

import com.example.loja_social.api.ApiService
import com.example.loja_social.api.BeneficiariosResponse
import com.example.loja_social.api.BeneficiarioRequest
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

    // [NOVO] Obtém um único beneficiário (não há endpoint, usamos a listagem como cache)
    // Na API real, seria um GET /api/admin/beneficiarios/{id}, mas a nossa lista já tem todos os dados.
    // Usaremos esta função para buscar o item no ViewModel de Detalhe.
    // **NOTA:** Esta função vai ser implementada no ViewModel de Detalhe com a lista já carregada,
    // mas a mantemos aqui para organizar futuros endpoints.

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