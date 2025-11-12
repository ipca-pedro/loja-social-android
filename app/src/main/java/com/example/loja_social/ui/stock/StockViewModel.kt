package com.example.loja_social.ui.stock

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loja_social.api.AddStockRequest
import com.example.loja_social.api.Categoria
import com.example.loja_social.api.Produto
import com.example.loja_social.repository.StockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class StockUiState(
    val isLoading: Boolean = true,
    val categorias: List<Categoria> = emptyList(),
    val produtos: List<Produto> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isFormLoading: Boolean = false
)

class StockViewModel(
    private val repository: StockRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StockUiState())
    val uiState: StateFlow<StockUiState> = _uiState

    init {
        fetchInitialData()
    }

    /**
     * Busca categorias e produtos assim que o ecrã arranca.
     */
    private fun fetchInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val categoriasResponse = repository.getCategorias()
                val produtosResponse = repository.getProdutos()

                if (categoriasResponse.success && produtosResponse.success) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        categorias = categoriasResponse.data,
                        produtos = produtosResponse.data
                    )
                } else {
                    val errorMsg = categoriasResponse.message ?: produtosResponse.message ?: "Erro ao carregar listas de categorias/produtos."
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = errorMsg)
                }
            } catch (e: Exception) {
                Log.e("StockVM", "Falha de rede ao carregar dados iniciais", e)
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Falha de ligação: ${e.message}")
            }
        }
    }

    /**
     * RF3: Envia o formulário para adicionar novo stock.
     */
    fun addStockItem(produtoId: Int, quantidade: String, dataValidade: String) {
        if (_uiState.value.isFormLoading) return

        // 1. Validação simples
        val quantidadeInt = quantidade.toIntOrNull()
        if (quantidadeInt == null || quantidadeInt <= 0) {
            _uiState.value = _uiState.value.copy(errorMessage = "A quantidade deve ser um número inteiro positivo.")
            return
        }

        // 2. Formatar data (se existir) - campo é opcional
        val dataFormatada = try {
            if (dataValidade.isNotBlank() && dataValidade.isNotEmpty()) {
                // Assume formato dd/MM/yyyy e converte para yyyy-MM-dd (formato API)
                val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = inputFormat.parse(dataValidade.trim())
                if (date != null) {
                    outputFormat.format(date)
                } else {
                    throw IllegalArgumentException("Data inválida")
                }
            } else {
                // Data é opcional, pode ser null
                null
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isFormLoading = false,
                errorMessage = "Formato de data inválido. Use DD/MM/AAAA ou deixe em branco."
            )
            return
        }

        val request = AddStockRequest(
            produtoId = produtoId,
            quantidadeInicial = quantidadeInt,
            dataValidade = dataFormatada,
            campanhaId = null // Implementação futura de seleção de campanha
        )

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isFormLoading = true, errorMessage = null, successMessage = null)
            try {
                val response = repository.addStock(request)
                if (response.success) {
                    val loteInfo = if (response.data?.id != null && response.data.id.length >= 4) {
                        "Lote: ${response.data.id.substring(0, 4)}..."
                    } else {
                        ""
                    }
                    val dataInfo = if (dataFormatada != null) {
                        " com validade até ${dataValidade}"
                    } else {
                        " (sem data de validade)"
                    }
                    _uiState.value = _uiState.value.copy(
                        isFormLoading = false,
                        successMessage = "Stock adicionado com sucesso! $loteInfo$dataInfo"
                    )
                } else {
                    val errorMsg = response.message ?: "Erro desconhecido ao adicionar stock."
                    _uiState.value = _uiState.value.copy(isFormLoading = false, errorMessage = errorMsg)
                }
            } catch (e: Exception) {
                Log.e("StockVM", "Erro de rede ao adicionar stock", e)
                _uiState.value = _uiState.value.copy(
                    isFormLoading = false,
                    errorMessage = "Falha de ligação: ${e.message ?: "Erro desconhecido"}"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}