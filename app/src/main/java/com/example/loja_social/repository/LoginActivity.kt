package com.example.loja_social.repository

import com.example.loja_social.api.ApiService
import com.example.loja_social.api.LoginRequest
import com.example.loja_social.api.LoginResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repositório que gere a lógica de autenticação.
 * Fica responsável por fazer a chamada à API.
 */
class LoginRepository(private val apiService: ApiService) {

    /**
     * Tenta fazer login na API.
     * @param email O email do utilizador.
     * @param password A password do utilizador.
     * @return O objeto LoginResponse da API.
     */
    suspend fun login(email: String, password: String): LoginResponse {
        // Criamos o objeto de pedido
        val request = LoginRequest(email, password)

        // Executamos a chamada na thread de IO (boa prática)
        // A ApiService é injetada (recebida) pelo construtor
        return withContext(Dispatchers.IO) {
            apiService.login(request)
        }
    }
}