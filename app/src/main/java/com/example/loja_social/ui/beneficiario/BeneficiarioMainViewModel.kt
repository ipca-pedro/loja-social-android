package com.example.loja_social.ui.beneficiario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loja_social.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BeneficiarioMainUiState(
    val isLoading: Boolean = false,
    val minhasEntregas: List<Entrega> = emptyList(),
    val campanhasAtivas: List<Campanha> = emptyList(),
    val errorMessage: String? = null
)

class BeneficiarioMainViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(BeneficiarioMainUiState())
    val uiState: StateFlow<BeneficiarioMainUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                // Carregar entregas do beneficiário (todas as entregas - API vai filtrar)
                val entregasResponse = apiService.getMinhasEntregas()
                
                // Carregar campanhas ativas (rota pública)
                val campanhasResponse = apiService.getCampanhas()
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    minhasEntregas = entregasResponse.data,
                    campanhasAtivas = campanhasResponse.data
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erro ao carregar dados: ${e.message}"
                )
            }
        }
    }

    fun refresh() {
        loadData()
    }
}