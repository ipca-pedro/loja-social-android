package com.example.loja_social.ui.stock

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loja_social.api.AddStockRequest
import com.example.loja_social.api.Campanha
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
 * @param isLoading Indica se está a carregar dados iniciais
 * @param categorias Lista de categorias disponíveis para seleção
 * @param produtos Lista de produtos disponíveis para seleção
 * @param campanhas Lista de campanhas disponíveis para seleção
 * @param errorMessage Mensagem de erro a exibir
 * @param successMessage Mensagem de sucesso a exibir
 * @param isFormLoading Indica se está a processar o envio do formulário
 */
data class StockUiState(
    val isLoading: Boolean = true,
    val categorias: List<Categoria> = emptyList(),
    val produtos: List<Produto> = emptyList(),
    val campanhas: List<Campanha> = emptyList(), // Novo campo
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isFormLoading: Boolean = false
)

/**
 * ViewModel para o formulário de adicionar novo stock.
 * Gerencia o carregamento de dados e o envio de novos lotes.
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
     * Busca todos os dados necessários (categorias, produtos, campanhas) para o formulário.
     */
    private fun fetchInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                // Carrega todos os dados em paralelo
                val categoriasResponse = repository.getCategorias()
                val produtosResponse = repository.getProdutos()
                val campanhasResponse = repository.getCampanhas() // Nova chamada

                if (categoriasResponse.success && produtosResponse.success && campanhasResponse.success) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        categorias = categoriasResponse.data,
                        produtos = produtosResponse.data,
                        campanhas = campanhasResponse.data // Guardar campanhas no estado
                    )
                } else {
                    val errorMessages = buildList {
                        if (!campanhasResponse.success) add("Erro ao carregar campanhas")
                        if (!categoriasResponse.success) add(categoriasResponse.message ?: "Erro ao carregar categorias")
                        if (!produtosResponse.success) add(produtosResponse.message ?: "Erro ao carregar produtos")
                    }
                    val errorMsg = errorMessages.joinToString("; ").ifEmpty { "Erro ao carregar dados." }
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
     * @param produtoId O ID do produto selecionado
     * @param quantidade A quantidade a adicionar
     * @param dataValidade A data de validade no formato DD/MM/AAAA (opcional)
     * @param campanhaId O ID da campanha selecionada (opcional)
     */
    fun addStockItem(produtoId: Int, quantidade: String, dataValidade: String, campanhaId: String?) {
        if (_uiState.value.isFormLoading) return

        val quantidadeInt = quantidade.toIntOrNull()
        if (quantidadeInt == null || quantidadeInt <= 0) {
            _uiState.value = _uiState.value.copy(errorMessage = "A quantidade deve ser um número inteiro positivo.")
            return
        }

        val dataFormatada = try {
            if (dataValidade.isNotBlank()) {
                val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = inputFormat.parse(dataValidade.trim())
                if (date != null) outputFormat.format(date) else throw IllegalArgumentException("Data inválida")
            } else {
                null
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(isFormLoading = false, errorMessage = "Formato de data inválido. Use DD/MM/AAAA.")
            return
        }

        val request = AddStockRequest(
            produtoId = produtoId,
            quantidadeInicial = quantidadeInt,
            dataValidade = dataFormatada,
            campanhaId = campanhaId // Passar o ID da campanha
        )

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isFormLoading = true, errorMessage = null, successMessage = null)
            try {
                val response = repository.addStock(request)
                if (response.success) {
                    val loteInfo = if (response.data?.id != null) "Lote: ${response.data.id.take(4)}..." else ""
                    val dataInfo = if (dataFormatada != null) " com validade até $dataValidade" else ""
                    _uiState.value = _uiState.value.copy(
                        isFormLoading = false,
                        successMessage = "Stock adicionado com sucesso! $loteInfo$dataInfo"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isFormLoading = false, errorMessage = response.message ?: "Erro ao adicionar stock.")
                }
            } catch (e: Exception) {
                Log.e("StockVM", "Erro de rede ao adicionar stock", e)
                _uiState.value = _uiState.value.copy(isFormLoading = false, errorMessage = "Falha de ligação: ${e.message}")
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }

    /**
     * Cria um novo produto e atualiza a lista de produtos.
     */
    fun createProduct(nome: String, descricao: String, categoriaId: Int) {
        if (_uiState.value.isFormLoading) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isFormLoading = true, errorMessage = null, successMessage = null)
            try {
                val request = com.example.loja_social.api.CreateProductRequest(nome, descricao, categoriaId)
                val response = repository.createProduct(request)
                
                if (response.success) {
                    // Atualizar a lista de produtos
                    val produtosResponse = repository.getProdutos()
                    if (produtosResponse.success) {
                        _uiState.value = _uiState.value.copy(
                            isFormLoading = false,
                            produtos = produtosResponse.data,
                            successMessage = "Produto '${response.data?.nome}' criado com sucesso!"
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isFormLoading = false, 
                            successMessage = "Produto criado, mas erro ao atualizar lista: ${produtosResponse.message}"
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isFormLoading = false,
                        errorMessage = response.message ?: "Erro ao criar produto."
                    )
                }
            } catch (e: Exception) {
                Log.e("StockVM", "Erro ao criar produto", e)
                _uiState.value = _uiState.value.copy(
                    isFormLoading = false,
                    errorMessage = "Falha de ligação: ${e.message}"
                )
            }
        }
    }
}