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

/**
 * ViewModel para a lista de beneficiários.
 * Gerencia a busca, pesquisa e filtragem de beneficiários.
 * Usa Flow.combine para reatividade automática quando pesquisa ou filtros mudam.
 */
class BeneficiariosViewModel(
    private val repository: BeneficiarioRepository
) : ViewModel() {

    /** Lista completa de beneficiários (sem filtros aplicados) */
    private val _allBeneficiarios = MutableStateFlow<List<Beneficiario>>(emptyList())
    
    /** Estado de pesquisa (texto digitado pelo utilizador) */
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    
    /** Estado de filtro por estado (null = todos, "ativo", "inativo") */
    private val _filterState = MutableStateFlow<String?>(null)
    val filterState: StateFlow<String?> = _filterState

    /**
     * Estado da UI com a lista filtrada e pesquisada.
     * Recalcula automaticamente quando qualquer um dos inputs muda.
     */
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

    /**
     * Busca a lista completa de beneficiários da API.
     * Atualiza o estado de loading e mensagens de erro.
     */
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

    /**
     * Atualiza o texto de pesquisa.
     * @param query O texto a pesquisar (será trimado automaticamente)
     */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query.trim()
    }

    /**
     * Define o filtro de estado (ativo/inativo).
     * @param state O estado a filtrar (null = mostrar todos, "ativo" ou "inativo")
     */
    fun setFilterState(state: String?) {
        _filterState.value = state
    }

    /**
     * Filtra e pesquisa a lista de beneficiários.
     * Aplica primeiro o filtro de estado, depois a pesquisa por texto.
     * 
     * @param all Lista completa de beneficiários
     * @param query Texto de pesquisa (pesquisa em nome, email, número de estudante e NIF)
     * @param filter Filtro de estado ("ativo", "inativo" ou null para todos)
     * @return Lista filtrada de beneficiários
     */
    private fun filterBeneficiarios(
        all: List<Beneficiario>,
        query: String,
        filter: String?
    ): List<Beneficiario> {
        var filtered = all

        // Aplica filtro de estado primeiro (se especificado)
        if (filter != null) {
            filtered = filtered.filter { beneficiario ->
                val estado = beneficiario.estado?.lowercase() ?: "inativo"
                estado == filter.lowercase()
            }
        }

        // Depois aplica pesquisa por texto (nome, email, número de estudante ou NIF)
        if (query.isNotEmpty()) {
            val queryLower = query.lowercase()
            filtered = filtered.filter { beneficiario ->
                beneficiario.nomeCompleto?.lowercase()?.contains(queryLower) == true ||
                beneficiario.email?.lowercase()?.contains(queryLower) == true ||
                beneficiario.numEstudante?.lowercase()?.contains(queryLower) == true ||
                beneficiario.nif?.contains(query) == true // NIF sem lowercase para manter formato
            }
        }

        return filtered
    }
}