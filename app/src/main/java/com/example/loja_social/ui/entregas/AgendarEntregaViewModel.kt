package com.example.loja_social.ui.entregas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loja_social.api.AgendarEntregaRequest
import com.example.loja_social.api.Beneficiario
import com.example.loja_social.repository.AgendarEntregaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AgendarEntregaUiState(
    val isLoading: Boolean = true,
    val isScheduling: Boolean = false,
    val beneficiarios: List<Beneficiario> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AgendarEntregaViewModel(
    private val repository: AgendarEntregaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgendarEntregaUiState())
    val uiState: StateFlow<AgendarEntregaUiState> = _uiState.asStateFlow()

    init {
        fetchBeneficiarios()
    }

    private fun fetchBeneficiarios() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = repository.getBeneficiarios()
                if (response.success) {
                    _uiState.update { it.copy(isLoading = false, beneficiarios = response.data ?: emptyList()) }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = response.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Falha de rede ao buscar beneficiários.") }
            }
        }
    }

    /**
     * RF4: Envia a requisição para agendar a entrega.
     * @param beneficiarioNomeCompleto O nome do item selecionado na lista (Beneficiario + N.º Estudante).
     * @param dataAgendamentoStr A data no formato DD/MM/AAAA.
     */
    /**
     * RF4: Envia a requisição para agendar a entrega, usando o ID diretamente.
     * @param beneficiarioId O ID (UUID) do beneficiário selecionado.
     * @param dataAgendamentoStr A data no formato DD/MM/AAAA.
     */
    fun agendarEntrega(beneficiarioId: String, dataAgendamentoStr: String) {
        if (_uiState.value.isScheduling) return

        // 1. Formatar e validar a data (usando a lógica do seu código original)
        val dataFormatada = try {
            val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
            java.time.LocalDate.parse(dataAgendamentoStr, formatter).format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(errorMessage = "Formato de data inválido. Use DD/MM/AAAA.")
            return
        }

        // 2. Criar a requisição
        val request = com.example.loja_social.api.AgendarEntregaRequest(
            beneficiarioId = beneficiarioId, // Corrigido: Usa o novo parâmetro
            dataAgendamento = dataFormatada,
            itens = emptyList()
        )

        // 3. Chamar a API e gerir o estado
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScheduling = true, errorMessage = null, successMessage = null)
            try {
                val response = repository.agendarEntrega(request)
                if (response.success) {
                    _uiState.value = _uiState.value.copy(
                        isScheduling = false,
                        successMessage = "Entrega agendada com sucesso! ID: ${response.data?.id?.substring(0, 4)}..."
                    )
                } else {
                    val errorMsg = response.message ?: "Erro desconhecido ao agendar entrega."
                    _uiState.value = _uiState.value.copy(isScheduling = false, errorMessage = errorMsg)
                }
            } catch (e: Exception) {
                android.util.Log.e("AgendarEntregaVM", "Erro de rede ao agendar entrega", e)
                _uiState.value = _uiState.value.copy(isScheduling = false, errorMessage = "Falha de ligação: ${e.message}")
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}