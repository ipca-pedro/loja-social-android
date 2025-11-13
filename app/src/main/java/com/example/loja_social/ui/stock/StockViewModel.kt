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

/**
 * Estado da UI do formulário de adicionar stock.
 * @param isLoading Indica se está a carregar categorias e produtos iniciais
 * @param categorias Lista de categorias disponíveis para seleção
 * @param produtos Lista de produtos disponíveis para seleção
 * @param errorMessage Mensagem de erro a exibir (null se não houver erro)
 * @param successMessage Mensagem de sucesso a exibir (null se não houver sucesso)
 * @param isFormLoading Indica se está a processar o envio do formulário
 */
data class StockUiState(
    val isLoading: Boolean = true,
    val categorias: List<Categoria> = emptyList(),
    val produtos: List<Produto> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isFormLoading: Boolean = false
)

/**
 * ViewModel para o formulário de adicionar novo stock.
 * Gerencia o carregamento de categorias e produtos, e o envio de novos lotes.
 */
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
     * Usado para popular os dropdowns do formulário.
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
     * Processa o envio do formulário para adicionar novo stock.
     * Valida os dados, formata a data (se fornecida) e envia a requisição à API.
     * 
     * @param produtoId O ID do produto selecionado
     * @param quantidade A quantidade a adicionar (deve ser um número inteiro positivo)
     * @param dataValidade A data de validade no formato DD/MM/AAAA (opcional, pode ser vazia)
     */
    fun addStockItem(produtoId: Int, quantidade: String, dataValidade: String) {
        if (_uiState.value.isFormLoading) return

        // Validação da quantidade
        val quantidadeInt = quantidade.toIntOrNull()
        if (quantidadeInt == null || quantidadeInt <= 0) {
            _uiState.value = _uiState.value.copy(errorMessage = "A quantidade deve ser um número inteiro positivo.")
            return
        }

        // Formatação da data (campo opcional)
        // Converte de DD/MM/AAAA (formato do utilizador) para yyyy-MM-dd (formato da API)
        val dataFormatada = try {
            if (dataValidade.isNotBlank() && dataValidade.isNotEmpty()) {
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

        // Cria a requisição
        val request = AddStockRequest(
            produtoId = produtoId,
            quantidadeInicial = quantidadeInt,
            dataValidade = dataFormatada,
            campanhaId = null // TODO: Implementar seleção de campanha no futuro
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

    /**
     * Limpa as mensagens de erro e sucesso do estado.
     * Útil para resetar o feedback visual após o utilizador interagir com a UI.
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}