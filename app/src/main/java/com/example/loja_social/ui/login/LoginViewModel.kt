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
 * Gerencia a autenticação do utilizador e o armazenamento do token de sessão.
 */
class LoginViewModel(
    private val loginRepository: LoginRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    /**
     * Processa o login do utilizador.
     * Valida os campos, autentica via API e guarda o token de sessão.
     * 
     * @param email Email do utilizador
     * @param password Password do utilizador
     */
    fun login(email: String, password: String) {
        // Previne múltiplas tentativas simultâneas
        if (_uiState.value == LoginUiState.Loading) return

        // Validação básica dos campos
        if (email.isEmpty() || password.isEmpty()) {
            _uiState.value = LoginUiState.Error("Por favor, preencha todos os campos.")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                val response = loginRepository.login(email, password)
                if (response.success && response.token != null) {
                    // Guarda o token para uso em requisições futuras
                    sessionManager.saveAuthToken(response.token)
                    Log.d("LoginViewModel", "Login bem-sucedido! Token guardado.")
                    _uiState.value = LoginUiState.Success
                } else {
                    Log.d("LoginViewModel", "Login falhou: ${response.message}")
                    _uiState.value = LoginUiState.Error(response.message ?: "Email ou password inválidos.")
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Erro de exceção durante o login", e)
                _uiState.value = LoginUiState.Error("Falha na ligação: ${e.message}")
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