package com.example.loja_social.ui.entregas

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loja_social.api.AgendarEntregaItemRequest
import com.example.loja_social.api.AgendarEntregaRequest
import com.example.loja_social.api.Beneficiario
import com.example.loja_social.repository.AgendarEntregaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
    val uiState: StateFlow<AgendarEntregaUiState> = _uiState

    init {
        fetchBeneficiarios()
    }

    private fun fetchBeneficiarios() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val response = repository.getBeneficiarios()
                if (response.success) {
                    // Filtra apenas beneficiários ativos
                    val ativos = response.data.filter { it.estado.equals("ativo", ignoreCase = true) }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        beneficiarios = ativos
                    )
                } else {
                    val errorMsg = response.message ?: "Erro ao carregar lista de beneficiários."
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = errorMsg)
                }
            } catch (e: Exception) {
                Log.e("AgendarEntregaVM", "Falha de rede ao carregar beneficiários", e)
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Falha de ligação: ${e.message}")
            }
        }
    }

    /**
     * RF4: Envia a requisição para agendar a entrega.
     * @param beneficiarioNomeCompleto O nome do item selecionado na lista (Beneficiario + N.º Estudante).
     * @param dataAgendamentoStr A data no formato DD/MM/AAAA.
     */
    fun agendarEntrega(beneficiarioNomeCompleto: String, dataAgendamentoStr: String) {
        if (_uiState.value.isScheduling) return

        // 1. Encontrar o ID do Beneficiário
        val beneficiarioEncontrado = _uiState.value.beneficiarios.find {
            "${it.nomeCompleto} (${it.numEstudante})" == beneficiarioNomeCompleto
        }

        if (beneficiarioEncontrado == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Beneficiário inválido ou não encontrado.")
            return
        }

        // 2. Formatar e validar a data
        val dataFormatada = try {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            LocalDate.parse(dataAgendamentoStr, formatter).format(DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(errorMessage = "Formato de data inválido. Use DD/MM/AAAA.")
            return
        }

        // 3. Criar a requisição (Itens vazios para simplificação)
        val request = AgendarEntregaRequest(
            beneficiarioId = beneficiarioEncontrado.id,
            dataAgendamento = dataFormatada,
            itens = emptyList() // O POST da API permite lista de itens vazia.
        )

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
                Log.e("AgendarEntregaVM", "Erro de rede ao agendar entrega", e)
                _uiState.value = _uiState.value.copy(isScheduling = false, errorMessage = "Falha de ligação: ${e.message}")
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}