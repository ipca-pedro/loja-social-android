package com.example.loja_social.ui.beneficiarios

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loja_social.api.Beneficiario
import com.example.loja_social.repository.BeneficiarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BeneficiariosViewModel(
    private val repository: BeneficiarioRepository
) : ViewModel() {

    // Lista completa de beneficiários (sem filtros)
    private val _allBeneficiarios = MutableStateFlow<List<Beneficiario>>(emptyList())
    
    // Estado de pesquisa
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    
    // Estado de filtro (null = todos, "ativo", "inativo")
    private val _filterState = MutableStateFlow<String?>(null)
    val filterState: StateFlow<String?> = _filterState

    // Lista filtrada e pesquisada (o que aparece na UI)
    val uiState: StateFlow<List<Beneficiario>> = combine(
        _allBeneficiarios,
        _searchQuery,
        _filterState
    ) { all: List<Beneficiario>, query: String, filter: String? ->
        filterBeneficiarios(all, query, filter)
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        fetchBeneficiarios()
    }

    fun fetchBeneficiarios() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = repository.getBeneficiarios()
                if (response.success) {
                    _allBeneficiarios.value = response.data
                    Log.d("BeneficiariosVM", "Carregados ${response.data.size} beneficiários.")
                } else {
                    Log.w("BeneficiariosVM", "API retornou erro: ${response.message}")
                    _errorMessage.value = response.message ?: "Erro desconhecido da API"
                }
            } catch (e: Exception) {
                Log.e("BeneficiariosVM", "Falha de rede", e)
                _errorMessage.value = "Falha na ligação: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query.trim()
    }

    fun setFilterState(state: String?) {
        _filterState.value = state
    }

    private fun filterBeneficiarios(
        all: List<Beneficiario>,
        query: String,
        filter: String?
    ): List<Beneficiario> {
        var filtered = all

        // Aplicar filtro de estado
        if (filter != null) {
            filtered = filtered.filter { beneficiario ->
                val estado = beneficiario.estado?.lowercase() ?: "inativo"
                estado == filter.lowercase()
            }
        }

        // Aplicar pesquisa
        if (query.isNotEmpty()) {
            val queryLower = query.lowercase()
            filtered = filtered.filter { beneficiario ->
                beneficiario.nomeCompleto?.lowercase()?.contains(queryLower) == true ||
                beneficiario.email?.lowercase()?.contains(queryLower) == true ||
                beneficiario.numEstudante?.lowercase()?.contains(queryLower) == true ||
                beneficiario.nif?.contains(query) == true
            }
        }

        return filtered
    }
}