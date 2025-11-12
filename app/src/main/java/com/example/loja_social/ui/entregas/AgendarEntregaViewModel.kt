package com.example.loja_social.ui.entregas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loja_social.api.AgendarEntregaRequest
import com.example.loja_social.api.Beneficiario
import com.example.loja_social.repository.AgendarEntregaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AgendarEntregaUiState(
    val isLoading: Boolean = true,
    val isScheduling: Boolean = false,
    val beneficiarios: List<Beneficiario> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AgendarEntregaViewModel(
    private val repository: AgendarEntregaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgendarEntregaUiState())
    val uiState: StateFlow<AgendarEntregaUiState> = _uiState.asStateFlow()

    init {
        fetchBeneficiarios()
    }

    private fun fetchBeneficiarios() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = repository.getBeneficiarios()
                if (response.success) {
                    _uiState.update { it.copy(isLoading = false, beneficiarios = response.data ?: emptyList()) }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = response.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Falha de rede ao buscar beneficiários.") }
            }
        }
    }

    fun agendarEntrega(beneficiarioId: String, data: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isScheduling = true, errorMessage = null, successMessage = null) }
            try {
                val request = AgendarEntregaRequest(beneficiarioId, data, emptyList()) // Lista de itens vazia por agora
                val response = repository.agendarEntrega(request)
                if (response.success) {
                    _uiState.update { it.copy(isScheduling = false, successMessage = "Entrega agendada com sucesso!") }
                } else {
                    _uiState.update { it.copy(isScheduling = false, errorMessage = response.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isScheduling = false, errorMessage = "Falha de rede ao agendar entrega.") }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}