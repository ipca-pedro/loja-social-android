package com.example.loja_social.ui.stock

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loja_social.api.LoteIndividual
import com.example.loja_social.api.StockItem
import com.example.loja_social.api.UpdateStockRequest
import com.example.loja_social.repository.StockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class StockDetailUiState(
    val isLoading: Boolean = true,
    val stockItem: StockItem? = null,
    val lotes: List<LoteIndividual> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null
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
                // Buscar dados agregados do produto
                val stockResponse = repository.getStock()
                val lotesResponse = repository.getLotesByProduto(produtoId)
                
                if (stockResponse.success && lotesResponse.success) {
                    // Encontrar o produto específico pelo ID
                    val produto = stockResponse.data.find { it.produtoId == produtoId }
                    if (produto != null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            stockItem = produto,
                            lotes = lotesResponse.data
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Produto não encontrado"
                        )
                    }
                } else {
                    val errorMsg = stockResponse.message ?: lotesResponse.message ?: "Erro ao carregar detalhes"
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

    fun updateLote(loteId: String, quantidadeAtual: Int, dataValidade: String?) {
        viewModelScope.launch {
            try {
                val request = UpdateStockRequest(quantidadeAtual, dataValidade)
                val response = repository.updateStock(loteId, request)
                if (response.success) {
                    _uiState.value = _uiState.value.copy(
                        successMessage = response.message ?: "Lote atualizado com sucesso"
                    )
                    // Recarregar dados
                    fetchStockDetail()
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = response.message ?: "Erro ao atualizar lote"
                    )
                }
            } catch (e: Exception) {
                Log.e("StockDetailVM", "Erro ao atualizar lote", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Falha de ligação: ${e.message ?: "Erro desconhecido"}"
                )
            }
        }
    }

    fun deleteLote(loteId: String) {
        viewModelScope.launch {
            try {
                val response = repository.deleteStock(loteId)
                if (response.success) {
                    _uiState.value = _uiState.value.copy(
                        successMessage = response.message ?: "Lote removido com sucesso"
                    )
                    // Recarregar dados
                    fetchStockDetail()
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = response.message ?: "Erro ao remover lote"
                    )
                }
            } catch (e: Exception) {
                Log.e("StockDetailVM", "Erro ao remover lote", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Falha de ligação: ${e.message ?: "Erro desconhecido"}"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }

    fun refresh() {
        fetchStockDetail()
    }
}

