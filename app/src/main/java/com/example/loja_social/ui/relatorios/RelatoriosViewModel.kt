package com.example.loja_social.ui.relatorios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loja_social.api.RetrofitInstance
import com.example.loja_social.api.RelatorioEntregaItem
import com.example.loja_social.api.RelatorioStockItem
import com.example.loja_social.api.RelatorioValidadeItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class RelatoriosUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val entregas: List<RelatorioEntregaItem> = emptyList(),
    val stock: List<RelatorioStockItem> = emptyList(),
    val validade: List<RelatorioValidadeItem> = emptyList(),
    val campanhas: List<com.example.loja_social.api.Campanha> = emptyList()
)

class RelatoriosViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RelatoriosUiState())
    val uiState: StateFlow<RelatoriosUiState> = _uiState.asStateFlow()

    fun fetchRelatorioEntregas(inicio: String?, fim: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                // Ensure initialization if not ready (though typically it is)
                // RetrofitInstance.initialize(context) // cannot call this here easily, assume main activity did it.
                
                if (RetrofitInstance.isInitialized()) {
                    val response = RetrofitInstance.api.getRelatorioEntregas(inicio, fim)
                    if (response.success) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            entregas = response.data
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = response.message ?: "Erro ao obter relatório de entregas"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erro de conexão: ${e.message}"
                )
            }
        }
    }

    fun fetchRelatorioStock(campanhaId: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                if (RetrofitInstance.isInitialized()) {
                    val response = RetrofitInstance.api.getRelatorioStock(campanhaId)
                    if (response.success) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            stock = response.data
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = response.message ?: "Erro ao obter relatório de stock"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erro de conexão: ${e.message}"
                )
            }
        }
    }

    fun fetchRelatorioValidade() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                if (RetrofitInstance.isInitialized()) {
                    val response = RetrofitInstance.api.getRelatorioValidade()
                    if (response.success) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            validade = response.data
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = response.message ?: "Erro ao obter relatório de validade"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erro de conexão: ${e.message}"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }

    fun fetchCampanhas() {
        viewModelScope.launch {
            try {
                if (RetrofitInstance.isInitialized()) {
                    val response = RetrofitInstance.api.getCampanhas()
                    if (response.success) {
                        _uiState.value = _uiState.value.copy(campanhas = response.data)
                    }
                }
            } catch (e: Exception) {
               // Ignore or log
            }
        }
    }
}
