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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StockDetailUiState(
    val isLoading: Boolean = true,
    val isOperationInProgress: Boolean = false, // Nova flag para operações pontuais
    val stockItem: StockItem? = null,
    val lotes: List<LoteIndividual> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val stockDataChanged: Boolean = false 
)

class StockDetailViewModel(
    private val repository: StockRepository,
    private val produtoId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(StockDetailUiState())
    val uiState: StateFlow<StockDetailUiState> = _uiState.asStateFlow()

    init {
        fetchStockDetail()
    }
    private fun fetchStockDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val stockResponse = repository.getStock()
                val lotesResponse = repository.getLotesByProduto(produtoId)

                if (stockResponse.success && lotesResponse.success) {
                    val produto = stockResponse.data.find { it.produtoId == produtoId }
                    if (produto != null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                stockItem = produto,
                                lotes = lotesResponse.data.sortedBy { lote -> lote.dataValidade }
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false, errorMessage = "Produto não encontrado") }
                    }
                } else {
                    val errorMsg = stockResponse.message ?: lotesResponse.message ?: "Erro ao carregar detalhes"
                    _uiState.update { it.copy(isLoading = false, errorMessage = errorMsg) }
                }
            } catch (e: Exception) {
                Log.e("StockDetailVM", "Falha ao carregar detalhes", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "Falha de ligação: ${e.message}") }
            }
        }
    }

    fun deleteLote(loteId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isOperationInProgress = true, errorMessage = null, successMessage = null) }
            try {
                val response = repository.deleteLote(loteId)
                if (response.success) {
                    val updatedLotes = _uiState.value.lotes.filterNot { it.id == loteId }
                    _uiState.update {
                        it.copy(
                            lotes = updatedLotes,
                            stockDataChanged = true,
                            successMessage = response.message ?: "Lote removido com sucesso",
                            isOperationInProgress = false
                        )
                    }
                    fetchStockItemData()
                } else {
                    _uiState.update { it.copy(isOperationInProgress = false, errorMessage = response.message ?: "Erro ao remover lote") }
                }
            } catch (e: Exception) {
                Log.e("StockDetailVM", "Erro ao remover lote", e)
                _uiState.update { it.copy(isOperationInProgress = false, errorMessage = "Falha de ligação: ${e.message}") }
            }
        }
    }

    fun reportDamagedUnit(lote: LoteIndividual) {
        if (lote.quantidadeAtual <= 0) {
            _uiState.update { it.copy(errorMessage = "Não existem unidades para remover.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isOperationInProgress = true, errorMessage = null, successMessage = null) }
            try {
                val response = repository.reportarDano(lote.id)

                if (response.success && response.data != null) {
                    val newData = response.data
                    
                    val updatedLotes = _uiState.value.lotes.map {
                        if (it.id == lote.id) {
                            it.copy(
                                quantidadeAtual = newData.quantidadeAtual,
                                quantidadeDanificada = newData.quantidadeDanificada
                            )
                        } else {
                            it
                        }
                    }
                    _uiState.update {
                        it.copy(
                            lotes = updatedLotes,
                            stockDataChanged = true,
                            successMessage = response.message ?: "Unidade danificada reportada com sucesso.",
                            isOperationInProgress = false
                        )
                    }
                    fetchStockItemData()
                } else {
                    _uiState.update { it.copy(isOperationInProgress = false, errorMessage = response.message ?: "Erro ao reportar unidade danificada.") }
                }
            } catch (e: Exception) {
                Log.e("StockDetailVM", "Erro ao reportar unidade danificada.", e)
                _uiState.update { it.copy(isOperationInProgress = false, errorMessage = "Falha de ligação: ${e.message}") }
            }
        }
    }

    // Função auxiliar para atualizar apenas os dados do stock item (ex: contagem total)
    private fun fetchStockItemData() {
        viewModelScope.launch {
            try {
                val stockResponse = repository.getStock()
                if (stockResponse.success) {
                    val produto = stockResponse.data.find { it.produtoId == produtoId }
                    _uiState.update { it.copy(stockItem = produto) }
                }
            } catch (e: Exception) {
                Log.e("StockDetailVM", "Falha ao recarregar stock item", e)
            }
        }
    }

    fun onRefreshHandled() {
        _uiState.update { it.copy(stockDataChanged = false) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}