package com.example.loja_social.repository

import com.example.loja_social.api.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DashboardRepository(private val apiService: ApiService) {

    // RF6: Alertas de Validade
    suspend fun getAlertasValidade() = withContext(Dispatchers.IO) {
        apiService.getAlertasValidade()
    }

    // RF4: Lista de Entregas (para contarmos as agendadas)
    suspend fun getEntregas() = withContext(Dispatchers.IO) {
        apiService.getEntregas()
    }
}