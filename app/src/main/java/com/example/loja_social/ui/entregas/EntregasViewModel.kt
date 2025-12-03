package com.example.loja_social.ui.entregas

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loja_social.api.Entrega
import com.example.loja_social.repository.EntregaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Estado da UI da lista de entregas.
 * @param isLoading Indica se está a carregar dados iniciais
 * @param errorMessage Mensagem de erro a exibir (null se não houver erro)
 * @param entregas Lista de entregas (agendadas e concluídas)
 * @param actionSuccessMessage Mensagem de sucesso após concluir uma entrega (null se não houver)
 */
data class EntregasUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val entregas: List<Entrega> = emptyList(),
    val actionSuccessMessage: String? = null
)

/**
 * ViewModel para a lista de entregas.
 * Gerencia a busca de entregas e a conclusão de entregas agendadas.
 */
class EntregasViewModel(
    private val repository: EntregaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EntregasUiState())
    val uiState: StateFlow<EntregasUiState> = _uiState

    init {
        fetchEntregas()
    }

    /**
     * Carrega a lista de todas as entregas (agendadas e concluídas).
     */
    fun fetchEntregas() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, actionSuccessMessage = null)
            try {
                val response = repository.getEntregas()
                if (response.success) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        entregas = response.data
                    )
                    Log.d("EntregasVM", "Carregadas ${response.data.size} entregas.")
                } else {
                    val errorMsg = response.message ?: "Erro desconhecido ao carregar entregas"
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = errorMsg)
                    Log.w("EntregasVM", "API retornou erro: $errorMsg")
                }
            } catch (e: Exception) {
                Log.e("EntregasVM", "Falha de rede", e)
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Falha de ligação: ${e.message}")
            }
        }
    }

    /**
     * Marca uma entrega como concluída.
     * O servidor automaticamente abate o stock dos itens entregues.
     * Após sucesso, recarrega a lista para mostrar o novo estado.
     * 
     * @param entregaId O ID (UUID) da entrega a concluir
     */
    fun concluirEntrega(entregaId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actionSuccessMessage = null) // Limpa msg de sucesso anterior
            try {
                val response = repository.concluirEntrega(entregaId)
                if (response.success) {
                    _uiState.value = _uiState.value.copy(
                        actionSuccessMessage = "Entrega ${entregaId.substring(0, 4)}... concluída com sucesso! Stock abatido."
                    )
                    fetchEntregas() // Recarrega a lista para mostrar o novo estado
                } else {
                    val errorMsg = response.message ?: "Erro ao concluir a entrega"
                    _uiState.value = _uiState.value.copy(errorMessage = errorMsg)
                    Log.e("EntregasVM", "Falha ao concluir entrega: $errorMsg")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Falha de rede ao concluir entrega: ${e.message}")
            }
        }
    }

    /**
     * Filtra entregas para mostrar apenas as de hoje.
     */
    fun filterByToday() {
        val today = java.time.LocalDate.now().toString()
        val currentEntregas = _uiState.value.entregas
        val todayEntregas = currentEntregas.filter { entrega ->
            entrega.dataAgendamento.startsWith(today)
        }
        _uiState.value = _uiState.value.copy(entregas = todayEntregas)
    }

    /**
     * Limpa a mensagem de sucesso da ação.
     */
    fun clearActionMessage() {
        _uiState.value = _uiState.value.copy(actionSuccessMessage = null)
    }
}