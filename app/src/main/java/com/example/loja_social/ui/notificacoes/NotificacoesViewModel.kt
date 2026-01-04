package com.example.loja_social.ui.notificacoes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.loja_social.api.Notificacao
import com.example.loja_social.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificacoesUiState(
    val notificacoes: List<Notificacao> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class NotificacoesViewModel(private val repository: NotificationRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificacoesUiState())
    val uiState: StateFlow<NotificacoesUiState> = _uiState.asStateFlow()

    init {
        fetchNotificacoes()
        markAllRead()
    }

    fun fetchNotificacoes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val response = repository.getNotificacoes()
            if (response.success && response.data != null) {
                _uiState.update { it.copy(notificacoes = response.data, isLoading = false) }
            } else {
                _uiState.update { it.copy(errorMessage = response.message ?: "Erro ao carregar notificações", isLoading = false) }
            }
        }
    }

    fun marcarComoLida(id: String) {
        viewModelScope.launch {
            val response = repository.marcarComoLida(id)
            if (response.success) {
                // Atualizar localmente
                _uiState.update { state ->
                    val updatedList = state.notificacoes.map { 
                        if (it.id == id) it.copy(lida = true) else it 
                    }
                    state.copy(notificacoes = updatedList)
                }
            } else {
                // Opcional: Mostrar erro
            }
        }
    }
    private fun markAllRead() {
        viewModelScope.launch {
            repository.marcarTodasComoLidas()
            // Atualizar UI local para refletir que tudo foi lido
            _uiState.update { state ->
                val updatedList = state.notificacoes.map { it.copy(lida = true) }
                state.copy(notificacoes = updatedList)
            }
        }
    }
}

class NotificacoesViewModelFactory(private val repository: NotificationRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificacoesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotificacoesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
