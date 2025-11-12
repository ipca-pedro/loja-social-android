package com.example.loja_social.ui.stock

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loja_social.api.StockItem
import com.example.loja_social.repository.StockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class StockListUiState(
    val isLoading: Boolean = true,
    val stockItems: List<StockItem> = emptyList(),
    val errorMessage: String? = null
)

class StockListViewModel(
    private val repository: StockRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StockListUiState())
    val uiState: StateFlow<StockListUiState> = _uiState

    init {
        fetchStock()
    }

    fun fetchStock() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val response = repository.getStock()
                if (response.success) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        stockItems = response.data
                    )
                    Log.d("StockListVM", "Carregados ${response.data.size} produtos em stock")
                } else {
                    val errorMsg = response.message ?: "Erro ao carregar stock"
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = errorMsg)
                }
            } catch (e: Exception) {
                Log.e("StockListVM", "Falha ao carregar stock", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Falha de ligação: ${e.message ?: "Erro desconhecido"}"
                )
            }
        }
    }

    fun refresh() {
        fetchStock()
    }
}

