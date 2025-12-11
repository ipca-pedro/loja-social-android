package com.example.loja_social.ui.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loja_social.SessionManager
import com.example.loja_social.repository.LoginRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.lang.Exception

/**
 * ViewModel para o ecrã de login.
 * Gerencia a autenticação do utilizador e o armazenamento da sessão.
 */
class LoginViewModel(
    private val loginRepository: LoginRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    /**
     * Processa o login do utilizador.
     * Valida os campos, autentica via API e guarda a sessão.
     * 
     * @param email Email do utilizador
     * @param password Password do utilizador
     * @param userType Tipo de utilizador ("admin" ou "beneficiario")
     */
    fun login(email: String, password: String, userType: String) {
        if (_uiState.value == LoginUiState.Loading) return

        if (email.isEmpty() || password.isEmpty()) {
            _uiState.value = LoginUiState.Error("Por favor, preencha todos os campos.")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            Log.d("LoginViewModel", "Iniciando login - Email: $email, UserType: $userType")
            try {
                val response = loginRepository.login(email, password, userType)
                Log.d("LoginViewModel", "Resposta da API - Success: ${response.success}, Token: ${response.token?.take(20)}..., Role: ${response.user?.role}")
                Log.d("LoginViewModel", "User: ${response.user}")
                
                if (response.success && response.token != null) {
                    val role = response.user?.role
                    sessionManager.saveAuthToken(response.token, role)
                    Log.d("LoginViewModel", "Token guardado! Role guardado: ${sessionManager.fetchUserRole()}")
                    _uiState.value = LoginUiState.Success
                } else {
                    Log.d("LoginViewModel", "Login falhou: ${response.message}")
                    _uiState.value = LoginUiState.Error(response.message ?: "Email ou password inválidos.")
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Erro de exceção durante o login", e)
                val errorMessage = when {
                    e.message?.contains("401") == true -> "Credenciais inválidas. Verifique o email e ${if (userType == "beneficiario") "NIF" else "password"}."
                    e.message?.contains("404") == true -> "Utilizador não encontrado."
                    e.message?.contains("500") == true -> "Erro no servidor. Tente novamente."
                    else -> "Falha na ligação: ${e.message}"
                }
                _uiState.value = LoginUiState.Error(errorMessage)
            }
        }
    }

    /**
     * Reseta o estado para Idle.
     * Útil para limpar mensagens de erro após o utilizador interagir com a UI.
     */
    fun resetStateToIdle() {
        _uiState.value = LoginUiState.Idle
    }
}