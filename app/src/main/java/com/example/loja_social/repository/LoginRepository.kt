package com.example.loja_social.repository

import com.example.loja_social.api.ApiService
import com.example.loja_social.api.LoginRequest

class LoginRepository(private val apiService: ApiService) {

    suspend fun login(email: String, password: String) = apiService.login(LoginRequest(email, password))

}