package com.example.loja_social.ui.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loja_social.data.repository.LoginRepository
import com.example.loja_social.data.session.SessionManager // Assumindo que moveste o SessionManager para data.session
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.lang.Exception

/**
 * ViewModel para a LoginActivity.
 * Contém toda a lógica de negócio e estado da UI para o ecrã de login.
 */
class LoginViewModel(
    private val loginRepository: LoginRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    // Estado interno mutável
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    // Estado público, apenas para leitura (para a Activity observar)
    val uiState: StateFlow<LoginUiState> = _uiState

    /**
     * Função que a Activity vai chamar para iniciar o processo de login.
     */
    fun login(email: String, password: String) {
        // Ignora se já estiver a carregar
        if (_uiState.value == LoginUiState.Loading) return

        // Validação simples
        if (email.isEmpty() || password.isEmpty()) {
            _uiState.value = LoginUiState.Error("Por favor, preencha todos os campos.")
            return
        }

        // Lançar a coroutine no scope do ViewModel
        viewModelScope.launch {
            // 1. Mudar o estado para Loading (mostra o ProgressBar na UI)
            _uiState.value = LoginUiState.Loading

            try {
                // 2. Chamar o Repositório (que chama a API)
                val response = loginRepository.login(email, password)

                // 3. Processar a resposta
                if (response.success && response.token != null) {
                    // SUCESSO!
                    // Guardar o token no nosso "cofre" (SessionManager)
                    sessionManager.saveAuthToken(response.token)
                    Log.d("LoginViewModel", "Login bem-sucedido! Token guardado.")
                    // Mudar o estado para Success (a UI vai reagir e navegar)
                    _uiState.value = LoginUiState.Success
                } else {
                    // FALHA (API disse success: false ou não enviou token)
                    Log.d("LoginViewModel", "Login falhou: ${response.message}")
                    _uiState.value = LoginUiState.Error(response.message ?: "Email ou password inválidos.")
                }

            } catch (e: Exception) {
                // ERRO (Ex: sem net, API offline, crash)
                Log.e("LoginViewModel", "Erro de exceção durante o login", e)
                _uiState.value = LoginUiState.Error("Falha na ligação: ${e.message}")
            }
        }
    }

    /**
     * Reseta o estado para Idle, caso a Activity queira
     * (ex: se o utilizador voltar atrás depois de um erro).
     */
    fun resetStateToIdle() {
        _uiState.value = LoginUiState.Idle
    }
}