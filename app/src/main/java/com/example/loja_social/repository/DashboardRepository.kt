package com.example.loja_social.repository

import com.example.loja_social.api.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository para operações relacionadas com o dashboard.
 * Centraliza chamadas à API para alertas de validade e entregas.
 */
class DashboardRepository(private val apiService: ApiService) {

    /**
     * Obtém a lista de alertas de validade (produtos a vencer em breve).
     * @return Resposta da API com lista de alertas
     */
    suspend fun getAlertasValidade() = withContext(Dispatchers.IO) {
        apiService.getAlertasValidade()
    }

    /**
     * Obtém a lista de todas as entregas.
     * Usado no dashboard para contar entregas agendadas.
     * @return Resposta da API com lista de entregas
     */
    suspend fun getEntregas() = withContext(Dispatchers.IO) {
        apiService.getEntregas()
    }
}