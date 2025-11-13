package com.example.loja_social.repository

import com.example.loja_social.api.ApiService
import com.example.loja_social.api.LoginRequest

/**
 * Repository para operações de autenticação.
 * Centraliza chamadas à API relacionadas com login.
 */
class LoginRepository(private val apiService: ApiService) {

    /**
     * Autentica um utilizador com email e password.
     * @param email Email do utilizador
     * @param password Password do utilizador
     * @return Resposta da API com token JWT se o login for bem-sucedido
     */
    suspend fun login(email: String, password: String) = apiService.login(LoginRequest(email, password))

}