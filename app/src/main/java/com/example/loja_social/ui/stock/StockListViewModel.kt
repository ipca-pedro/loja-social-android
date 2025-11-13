package com.example.loja_social.ui.stock

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loja_social.api.StockItem
import com.example.loja_social.repository.StockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Estado da UI da lista de stock.
 * @param isLoading Indica se está a carregar dados iniciais
 * @param stockItems Lista de itens de stock (filtrados e pesquisados)
 * @param categories Lista de categorias únicas para o dropdown de filtro
 * @param errorMessage Mensagem de erro a exibir (null se não houver erro)
 */
data class StockListUiState(
    val isLoading: Boolean = true,
    val stockItems: List<StockItem> = emptyList(),
    val categories: List<String> = emptyList(),
    val errorMessage: String? = null
)

/**
 * ViewModel para a lista de stock.
 * Gerencia a busca, pesquisa e filtragem de itens de stock.
 * Suporta filtros por validade próxima, stock baixo e categoria.
 */
class StockListViewModel(
    private val repository: StockRepository
) : ViewModel() {

    /** Lista completa de itens de stock (sem filtros aplicados) */
    private val _allStockItems = MutableStateFlow<List<StockItem>>(emptyList())
    /** Texto de pesquisa */
    private val _searchQuery = MutableStateFlow("")
    /** Tipo de filtro ("validade_proxima", "stock_baixo" ou null) */
    private val _filterType = MutableStateFlow<String?>(null)
    /** Filtro por categoria (nome da categoria ou null para todas) */
    private val _categoryFilter = MutableStateFlow<String?>(null)

    val uiState: StateFlow<StockListUiState> = combine(
        _allStockItems,
        _searchQuery,
        _filterType,
        _categoryFilter
    ) { all: List<StockItem>, query: String, filter: String?, category: String? ->
        val filteredItems = filterStockItems(all, query, filter, category)
        val categories = all.mapNotNull { it.categoria }.distinct().sorted()
        StockListUiState(
            isLoading = false,
            stockItems = filteredItems,
            categories = categories,
            errorMessage = null
        )
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = StockListUiState()
    )

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        fetchStock()
    }

    /**
     * Busca a lista completa de stock da API.
     * Atualiza o estado de loading e mensagens de erro.
     */
    fun fetchStock() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = repository.getStock()
                if (response.success) {
                    _allStockItems.value = response.data
                } else {
                    _errorMessage.value = response.message ?: "Erro ao carregar stock"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Falha de ligação: ${e.message ?: "Erro desconhecido"}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Atualiza o texto de pesquisa.
     * @param query O texto a pesquisar (será trimado automaticamente)
     */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query.trim()
    }

    /**
     * Define o tipo de filtro a aplicar.
     * @param filter "validade_proxima" (vence em 30 dias), "stock_baixo" (< 10 unidades) ou null (todos)
     */
    fun setFilterType(filter: String?) {
        _filterType.value = filter
    }

    /**
     * Define o filtro por categoria.
     * @param category O nome da categoria a filtrar ou null para mostrar todas
     */
    fun setCategoryFilter(category: String?) {
        _categoryFilter.value = category
    }

    /**
     * Filtra e pesquisa a lista de itens de stock.
     * Aplica pesquisa por texto, filtro de categoria e filtro de tipo (validade/stock baixo).
     * 
     * @param all Lista completa de itens
     * @param query Texto de pesquisa (pesquisa em nome do produto e categoria)
     * @param filter Tipo de filtro ("validade_proxima" ou "stock_baixo")
     * @param category Categoria a filtrar (null = todas)
     * @return Lista filtrada de itens
     */
    private fun filterStockItems(
        all: List<StockItem>,
        query: String,
        filter: String?,
        category: String?
    ): List<StockItem> {
        var filtered = all

        // Aplica pesquisa por texto primeiro (nome do produto ou categoria)
        if (query.isNotEmpty()) {
            val queryLower = query.lowercase()
            filtered = filtered.filter { item ->
                item.produto.lowercase().contains(queryLower) ||
                item.categoria?.lowercase()?.contains(queryLower) == true
            }
        }

        // Aplica filtro de categoria
        if (category != null) {
            filtered = filtered.filter { it.categoria == category }
        }

        // Aplica filtros de tipo (validade próxima ou stock baixo)
        when (filter) {
            "validade_proxima" -> {
                filtered = filtered.filter { item ->
                    item.validadeProxima != null && isExpiringSoon(item.validadeProxima)
                }
            }
            "stock_baixo" -> {
                filtered = filtered.filter { it.quantidadeTotal < 10 }
            }
        }

        return filtered
    }

    /**
     * Verifica se uma data de validade está próxima (dentro de 30 dias).
     * @param validityDate Data no formato yyyy-MM-dd
     * @return true se a data estiver entre hoje e 30 dias no futuro
     */
    private fun isExpiringSoon(validityDate: String): Boolean {
        return try {
            // A API retorna datas no formato yyyy-MM-dd
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = inputFormat.parse(validityDate)
            if (date != null) {
                val daysUntilExpiry = ((date.time - Date().time) / (1000 * 60 * 60 * 24)).toInt()
                daysUntilExpiry in 0..30
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Recarrega a lista de stock da API.
     * Útil para atualizar após adicionar/editar/remover stock.
     */
    fun refresh() {
        fetchStock()
    }
}
