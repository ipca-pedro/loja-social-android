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
    val errorMessage: String? = null
)

class StockListViewModel(
    private val repository: StockRepository
) : ViewModel() {

    // Lista completa de stock (sem filtros)
    private val _allStockItems = MutableStateFlow<List<StockItem>>(emptyList())
    
    // Estado de pesquisa
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    
    // Estado de filtro (null = todos, "validade_proxima", "stock_baixo")
    private val _filterType = MutableStateFlow<String?>(null)
    val filterType: StateFlow<String?> = _filterType

    // Lista filtrada e pesquisada (o que aparece na UI)
    val uiState: StateFlow<StockListUiState> = combine(
        _allStockItems,
        _searchQuery,
        _filterType
    ) { all: List<StockItem>, query: String, filter: String? ->
        val filtered = filterStockItems(all, query, filter)
        StockListUiState(
            isLoading = false,
            stockItems = filtered,
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
                    Log.d("StockListVM", "Carregados ${response.data.size} produtos em stock")
                } else {
                    val errorMsg = response.message ?: "Erro ao carregar stock"
                    _errorMessage.value = errorMsg
                }
            } catch (e: Exception) {
                Log.e("StockListVM", "Falha ao carregar stock", e)
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

    private fun filterStockItems(
        all: List<StockItem>,
        query: String,
        filter: String?
    ): List<StockItem> {
        var filtered = all

        // Aplicar pesquisa
        if (query.isNotEmpty()) {
            val queryLower = query.lowercase()
            filtered = filtered.filter { item ->
                item.produto.lowercase().contains(queryLower) ||
                item.categoria?.lowercase()?.contains(queryLower) == true
            }
        }

        // Aplicar filtros
        when (filter) {
            "validade_proxima" -> {
                filtered = filtered.filter { item ->
                    item.validadeProxima != null && isExpiringSoon(item.validadeProxima)
                }
            }
            "stock_baixo" -> {
                // Considerar stock baixo se quantidade < 10 (pode ser ajustado)
                filtered = filtered.filter { it.quantidadeTotal < 10 }
            }
        }

        return filtered
    }

    private fun isExpiringSoon(validityDate: String): Boolean {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = inputFormat.parse(validityDate)
            if (date != null) {
                val daysUntilExpiry = ((date.time - Date().time) / (1000 * 60 * 60 * 24)).toInt()
                daysUntilExpiry in 0..30 // Próximos 30 dias
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

