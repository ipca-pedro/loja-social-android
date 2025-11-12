package com.example.loja_social.ui.stock

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loja_social.api.StockItem
import com.example.loja_social.repository.StockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class StockDetailUiState(
    val isLoading: Boolean = true,
    val stockItem: StockItem? = null,
    val errorMessage: String? = null
)

class StockDetailViewModel(
    private val repository: StockRepository,
    private val produtoId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(StockDetailUiState())
    val uiState: StateFlow<StockDetailUiState> = _uiState

    init {
        fetchStockDetail()
    }

    private fun fetchStockDetail() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val response = repository.getStock()
                if (response.success) {
                    // Encontrar o produto específico pelo ID
                    val produto = response.data.find { it.produtoId == produtoId }
                    if (produto != null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            stockItem = produto
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Produto não encontrado"
                        )
                    }
                } else {
                    val errorMsg = response.message ?: "Erro ao carregar detalhes"
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = errorMsg)
                }
            } catch (e: Exception) {
                Log.e("StockDetailVM", "Falha ao carregar detalhes", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Falha de ligação: ${e.message ?: "Erro desconhecido"}"
                )
            }
        }
    }
}

