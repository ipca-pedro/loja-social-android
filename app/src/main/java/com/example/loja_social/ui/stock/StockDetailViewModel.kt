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

/**
 * Estado da UI da tela de detalhes de um produto de stock.
 * @param isLoading Indica se está a carregar dados iniciais
 * @param stockItem Dados agregados do produto (quantidade total, categoria, etc.)
 * @param lotes Lista de lotes individuais deste produto
 * @param errorMessage Mensagem de erro a exibir (null se não houver erro)
 * @param successMessage Mensagem de sucesso a exibir (null se não houver sucesso)
 */
data class StockDetailUiState(
    val isLoading: Boolean = true,
    val stockItem: StockItem? = null,
    val lotes: List<LoteIndividual> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null
)

/**
 * ViewModel para a tela de detalhes de um produto de stock.
 * Gerencia a exibição de dados agregados e lotes individuais, e operações CRUD nos lotes.
 * 
 * @param repository Repository para operações de stock
 * @param produtoId O ID do produto a exibir
 */
class StockDetailViewModel(
    private val repository: StockRepository,
    private val produtoId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(StockDetailUiState())
    val uiState: StateFlow<StockDetailUiState> = _uiState

    init {
        fetchStockDetail()
    }

    /**
     * Busca os dados do produto e seus lotes individuais.
     * Faz duas chamadas à API: uma para dados agregados e outra para lotes individuais.
     */
    private fun fetchStockDetail() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                // Busca dados agregados do produto (quantidade total, categoria, etc.)
                val stockResponse = repository.getStock()
                // Busca lotes individuais deste produto
                val lotesResponse = repository.getLotesByProduto(produtoId)
                
                if (stockResponse.success && lotesResponse.success) {
                    // Encontra o produto específico pelo ID na lista agregada
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

    /**
     * Atualiza um lote de stock existente.
     * Permite alterar a quantidade e a data de validade.
     * Após sucesso, recarrega os dados para refletir as mudanças.
     * 
     * @param loteId O ID (UUID) do lote a atualizar
     * @param quantidadeAtual A nova quantidade do lote
     * @param dataValidade A nova data de validade (formato yyyy-MM-dd ou null)
     */
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

    /**
     * Remove um lote de stock.
     * Após sucesso, recarrega os dados para remover o lote da lista.
     * 
     * @param loteId O ID (UUID) do lote a remover
     */
    fun deleteLote(loteId: String) {
        viewModelScope.launch {
            try {
                val response = repository.deleteStock(loteId)
                if (response.success) {
                    _uiState.value = _uiState.value.copy(
                        successMessage = response.message ?: "Lote removido com sucesso"
                    )
                    // Recarrega os dados para atualizar a lista
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

    /**
     * Limpa as mensagens de erro e sucesso do estado.
     * Útil para resetar o feedback visual após o utilizador interagir com a UI.
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }

    /**
     * Recarrega os dados do produto e seus lotes.
     * Útil para atualizar após operações externas ou pull-to-refresh.
     */
    fun refresh() {
        fetchStockDetail()
    }
}

