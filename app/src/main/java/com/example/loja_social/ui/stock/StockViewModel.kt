package com.example.loja_social.ui.stock

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loja_social.api.AddStockRequest
import com.example.loja_social.api.Campanha
import com.example.loja_social.api.Categoria
import com.example.loja_social.api.CreateProductRequest
import com.example.loja_social.api.Produto
import com.example.loja_social.repository.StockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

data class StockUiState(
    val isLoading: Boolean = true,
    val categorias: List<Categoria> = emptyList(),
    val produtos: List<Produto> = emptyList(),
    val campanhas: List<Campanha> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isFormLoading: Boolean = false,
    val stockDataChanged: Boolean = false // Flag para indicar que o stock foi alterado
)

class StockViewModel(
    private val repository: StockRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StockUiState())
    val uiState: StateFlow<StockUiState> = _uiState

    init {
        viewModelScope.launch {
            fetchInitialData()
        }
    }

    private suspend fun fetchInitialData() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        try {
            val categoriasResponse = repository.getCategorias()
            val produtosResponse = repository.getProdutos()
            val campanhasResponse = repository.getCampanhas()

            if (categoriasResponse.success && produtosResponse.success && campanhasResponse.success) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        categorias = categoriasResponse.data,
                        produtos = produtosResponse.data,
                        campanhas = campanhasResponse.data
                    )
                }
            } else {
                val errorMessages = buildList {
                    if (!campanhasResponse.success) add("Erro ao carregar campanhas")
                    if (!categoriasResponse.success) add(categoriasResponse.message ?: "Erro ao carregar categorias")
                    if (!produtosResponse.success) add(produtosResponse.message ?: "Erro ao carregar produtos")
                }
                val errorMsg = errorMessages.joinToString("; ").ifEmpty { "Erro ao carregar dados." }
                _uiState.update { it.copy(isLoading = false, errorMessage = errorMsg) }
            }
        } catch (e: Exception) {
            Log.e("StockVM", "Falha de rede ao carregar dados iniciais", e)
            _uiState.update { it.copy(isLoading = false, errorMessage = "Falha de ligação: ${e.message}") }
        }
    }

    fun addStockItem(produtoId: Int, quantidade: String, dataValidade: String, campanhaId: String?) {
        val quantidadeInt = quantidade.toIntOrNull()
        if (quantidadeInt == null || quantidadeInt <= 0) {
            _uiState.update { it.copy(errorMessage = "A quantidade deve ser um número inteiro positivo.") }
            return
        }

        val dataFormatada = try {
            if (dataValidade.isNotBlank()) {
                val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                inputFormat.parse(dataValidade.trim())?.let { outputFormat.format(it) }
            } else {
                null
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(errorMessage = "Formato de data inválido. Use DD/MM/AAAA.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isFormLoading = true, errorMessage = null, successMessage = null) }
            try {
                val request = AddStockRequest(produtoId, quantidadeInt, dataFormatada, campanhaId)
                val response = repository.addStock(request)
                if (response.success) {
                    _uiState.update { it.copy(stockDataChanged = true) } // Aciona o evento
                } else {
                    _uiState.update { it.copy(errorMessage = response.message ?: "Erro ao adicionar stock.") }
                }
            } catch (e: Exception) {
                Log.e("StockVM", "Erro de rede ao adicionar stock", e)
                _uiState.update { it.copy(errorMessage = "Falha de ligação: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isFormLoading = false) }
            }
        }
    }

    fun createProduct(nome: String, descricao: String?, categoriaId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isFormLoading = true, errorMessage = null, successMessage = null) }
            try {
                val request = CreateProductRequest(nome, descricao, categoriaId)
                val response = repository.createProduct(request)
                if (response.success && response.data != null) {
                    val newProductList = _uiState.value.produtos + response.data
                    _uiState.update {
                        it.copy(
                            produtos = newProductList.sortedBy { p -> p.nome },
                            successMessage = response.message ?: "Produto criado com sucesso!"
                        )
                    }
                } else {
                    _uiState.update { it.copy(errorMessage = response.message ?: "Erro ao criar produto.") }
                }
            } catch (e: Exception) {
                Log.e("StockVM", "Erro de rede ao criar produto", e)
                _uiState.update { it.copy(errorMessage = "Falha de ligação: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isFormLoading = false) }
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