package com.example.loja_social.ui.entregas

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loja_social.api.Entrega
import com.example.loja_social.repository.EntregaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class EntregaFilterType {
    AGENDADAS,
    ENTREGUES
}

data class EntregasUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val allEntregas: List<Entrega> = emptyList(),
    val filteredEntregas: List<Entrega> = emptyList(),
    val actionSuccessMessage: String? = null,
    val selectedTab: EntregaFilterType = EntregaFilterType.AGENDADAS
)

class EntregasViewModel(
    private val repository: EntregaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EntregasUiState())
    val uiState: StateFlow<EntregasUiState> = _uiState.asStateFlow()

    private var dataHasChanged = false

    init {
        fetchEntregas()
    }

    fun fetchEntregas() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, actionSuccessMessage = null) }
            try {
                val response = repository.getEntregas()
                if (response.success) {
                    val sortedEntregas = response.data.sortedBy { it.dataAgendamento }
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            allEntregas = sortedEntregas
                        )
                    }
                    filterEntregas(_uiState.value.selectedTab)
                    Log.d("EntregasVM", "Carregadas ${response.data.size} entregas.")
                } else {
                    val errorMsg = response.message ?: "Erro desconhecido ao carregar entregas"
                    _uiState.update { it.copy(isLoading = false, errorMessage = errorMsg) }
                    Log.w("EntregasVM", "API retornou erro: $errorMsg")
                }
            } catch (e: Exception) {
                Log.e("EntregasVM", "Falha de rede", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "Falha de ligação: ${e.message}") }
            }
        }
    }

    fun selectTab(tab: EntregaFilterType) {
        _uiState.update { it.copy(selectedTab = tab) }
        filterEntregas(tab)
    }

    private fun filterEntregas(tab: EntregaFilterType) {
        val filtered = when (tab) {
            EntregaFilterType.AGENDADAS -> _uiState.value.allEntregas.filter { it.estado == "agendada" }
            EntregaFilterType.ENTREGUES -> _uiState.value.allEntregas.filter { it.estado == "entregue" }
        }
        _uiState.update { it.copy(filteredEntregas = filtered) }
    }

    fun concluirEntrega(entregaId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionSuccessMessage = null) }
            try {
                val response = repository.concluirEntrega(entregaId)
                if (response.success) {
                    dataHasChanged = true // Marca que os dados mudaram
                    _uiState.update { 
                        it.copy(
                            actionSuccessMessage = "Entrega concluída com sucesso! Stock abatido."
                        )
                    }
                    fetchEntregas() // Recarrega a lista para mostrar o novo estado
                } else {
                    val errorMsg = response.message ?: "Erro ao concluir a entrega"
                    _uiState.update { it.copy(errorMessage = errorMsg) }
                    Log.e("EntregasVM", "Falha ao concluir entrega: $errorMsg")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Falha de rede ao concluir entrega: ${e.message}") }
            }
        }
    }

    fun cancelarEntrega(entregaId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionSuccessMessage = null) }
            try {
                val response = repository.cancelarEntrega(entregaId)
                if (response.success) {
                    dataHasChanged = true
                    _uiState.update { 
                        it.copy(actionSuccessMessage = "Entrega cancelada e stock libertado!")
                    }
                    fetchEntregas() 
                } else {
                    val errorMsg = response.message ?: "Erro ao cancelar entrega"
                    _uiState.update { it.copy(errorMessage = errorMsg) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Erro de rede: ${e.message}") }
            }
        }
    }

    /**
     * Verifica se o dashboard precisa de ser atualizado.
     * @return True se os dados mudaram, False caso contrário. O estado é reiniciado após a chamada.
     */
    fun needsDashboardRefresh(): Boolean {
        val needsRefresh = dataHasChanged
        dataHasChanged = false // Reinicia o estado
        return needsRefresh
    }

    fun filterByToday() {
        val today = java.time.LocalDate.now().toString()
        val todayEntregas = _uiState.value.allEntregas.filter { entrega ->
            entrega.dataAgendamento.startsWith(today) && entrega.estado == "agendada"
        }
        _uiState.update { it.copy(filteredEntregas = todayEntregas, selectedTab = EntregaFilterType.AGENDADAS) }
    }

    fun clearActionMessage() {
        _uiState.update { it.copy(actionSuccessMessage = null) }
    }
}