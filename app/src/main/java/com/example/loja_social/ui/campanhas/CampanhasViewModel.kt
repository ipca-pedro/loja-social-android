package com.example.loja_social.ui.campanhas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.loja_social.api.Campanha
import com.example.loja_social.repository.CampanhaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CampanhasUiState(
    val campanhas: List<Campanha> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class CampanhasViewModel(private val repository: CampanhaRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CampanhasUiState())
    val uiState: StateFlow<CampanhasUiState> = _uiState.asStateFlow()

    init {
        loadCampanhas()
    }

    fun loadCampanhas() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.getCampanhas()
            result.onSuccess { campanhas ->
                _uiState.value = _uiState.value.copy(
                    campanhas = campanhas,
                    isLoading = false
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message
                )
            }
        }
    }

    fun createCampanha(nome: String, descricao: String, dataInicio: String, dataFim: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            val result = repository.createCampanha(nome, descricao, dataInicio, dataFim)
            result.onSuccess {
                loadCampanhas() // Reload list
                _uiState.value = _uiState.value.copy(successMessage = "Campanha criada com sucesso!")
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message
                )
            }
        }
    }

    fun updateCampanha(id: String, nome: String, descricao: String, dataInicio: String, dataFim: String, ativo: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            val result = repository.updateCampanha(id, nome, descricao, dataInicio, dataFim, ativo)
            result.onSuccess {
                loadCampanhas()
                _uiState.value = _uiState.value.copy(successMessage = "Campanha atualizada com sucesso!")
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message
                )
            }
        }
    }

    fun deleteCampanha(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            val result = repository.deleteCampanha(id)
            result.onSuccess {
                loadCampanhas()
                _uiState.value = _uiState.value.copy(successMessage = "Campanha removida com sucesso!")
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}

class CampanhasViewModelFactory(private val repository: CampanhaRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CampanhasViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CampanhasViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
