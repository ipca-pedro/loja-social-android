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

    private val _allBeneficiarios = MutableStateFlow<List<Beneficiario>>(emptyList())
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    
    private val _filterState = MutableStateFlow<String?>(null)
    val filterState: StateFlow<String?> = _filterState

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
                } else {
                    _errorMessage.value = response.message ?: "Erro desconhecido da API"
                }
            } catch (e: Exception) {
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

        if (filter != null) {
            filtered = filtered.filter { beneficiario ->
                val estado = beneficiario.estado.lowercase()
                estado == filter.lowercase()
            }
        }

        if (query.isNotEmpty()) {
            val queryLower = query.lowercase()
            filtered = filtered.filter { beneficiario ->
                beneficiario.nomeCompleto.lowercase().contains(queryLower) ||
                beneficiario.email?.lowercase()?.contains(queryLower) == true ||
                beneficiario.numEstudante?.lowercase()?.contains(queryLower) == true ||
                beneficiario.nif?.contains(query) == true
            }
        }

        return filtered
    }
}