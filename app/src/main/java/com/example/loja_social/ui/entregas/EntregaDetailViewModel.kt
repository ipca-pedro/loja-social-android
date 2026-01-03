package com.example.loja_social.ui.entregas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loja_social.api.EntregaDetailItem
import com.example.loja_social.repository.EntregaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EntregaDetailUiState(
    val isLoading: Boolean = true,
    val entregaItems: List<EntregaDetailItem> = emptyList(),
    val errorMessage: String? = null
)

class EntregaDetailViewModel(private val entregaId: String, private val repository: EntregaRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(EntregaDetailUiState())
    val uiState: StateFlow<EntregaDetailUiState> = _uiState.asStateFlow()

    init {
        fetchEntregaDetails()
    }

    private fun fetchEntregaDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = repository.getEntregaDetails(entregaId)
                if (response.success) {
                    _uiState.update { it.copy(isLoading = false, entregaItems = response.data ?: emptyList()) }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = response.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }
}