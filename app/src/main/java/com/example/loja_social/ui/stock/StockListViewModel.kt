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

data class StockListUiState(
    val isLoading: Boolean = true,
    val stockItems: List<StockItem> = emptyList(),
    val categories: List<String> = emptyList(), // NOVO: Lista de categorias para o dropdown
    val errorMessage: String? = null
)

class StockListViewModel(
    private val repository: StockRepository
) : ViewModel() {

    private val _allStockItems = MutableStateFlow<List<StockItem>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _filterType = MutableStateFlow<String?>(null)
    private val _categoryFilter = MutableStateFlow<String?>(null) // NOVO: Filtro de categoria

    val uiState: StateFlow<StockListUiState> = combine(
        _allStockItems,
        _searchQuery,
        _filterType,
        _categoryFilter // NOVO: Adicionado ao combine
    ) { all, query, filter, category ->
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

    fun setSearchQuery(query: String) {
        _searchQuery.value = query.trim()
    }

    fun setFilterType(filter: String?) {
        _filterType.value = filter
    }

    fun setCategoryFilter(category: String?) { // NOVO
        _categoryFilter.value = category
    }

    private fun filterStockItems(
        all: List<StockItem>,
        query: String,
        filter: String?,
        category: String? // NOVO
    ): List<StockItem> {
        var filtered = all

        if (query.isNotEmpty()) {
            val queryLower = query.lowercase()
            filtered = filtered.filter { item ->
                item.produto.lowercase().contains(queryLower) ||
                item.categoria?.lowercase()?.contains(queryLower) == true
            }
        }

        if (category != null) { // NOVO: Aplicar filtro de categoria
            filtered = filtered.filter { it.categoria == category }
        }

        when (filter) {
            "validade_proxima" -> {
                filtered = filtered.filter { item ->
                    item.validadeProxima != null && isExpiringSoon(item.validadeProxima)
                }
            }
            "stock_baixo" -> {
                filtered = filtered.filter { (it.quantidadeTotal?.toInt() ?: 0) < 10 }
            }
        }

        return filtered
    }

    private fun isExpiringSoon(validityDate: String): Boolean {
        // ... (código existente)
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
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

    fun refresh() {
        fetchStock()
    }
}
