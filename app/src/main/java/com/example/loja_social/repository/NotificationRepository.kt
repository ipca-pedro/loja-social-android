package com.example.loja_social.repository

import com.example.loja_social.api.ApiService
import com.example.loja_social.api.ApiResponse
import com.example.loja_social.api.Notificacao

class NotificationRepository(private val apiService: ApiService) {

    suspend fun getNotificacoes(): ApiResponse<List<Notificacao>> {
        return try {
            apiService.getNotificacoes()
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message, data = null)
        }
    }

    suspend fun marcarComoLida(id: String): ApiResponse<Notificacao> {
        return try {
            apiService.marcarNotificacaoLida(id)
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message, data = null)
        }
    }

    suspend fun marcarTodasComoLidas(): ApiResponse<Any> {
        return try {
            apiService.marcarTodasNotificacoesLidas()
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message, data = null)
        }
    }
}
