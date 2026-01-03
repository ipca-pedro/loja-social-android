package com.example.loja_social.ui.entregas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loja_social.api.AgendarEntregaItemRequest
import com.example.loja_social.api.AgendarEntregaRequest
import com.example.loja_social.api.Beneficiario
import com.example.loja_social.api.LoteIndividual
import com.example.loja_social.repository.AgendarEntregaRepository
import com.example.loja_social.repository.StockRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ItemSelecionado(
    val lote: LoteIndividual,
    val quantidade: Int
)

data class AgendarEntregaUiState(
    val isLoading: Boolean = false,
    val isScheduling: Boolean = false,
    val beneficiarios: List<Beneficiario> = emptyList(),
    val lotesDisponiveis: List<LoteIndividual> = emptyList(),
    val itensSelecionados: List<ItemSelecionado> = emptyList(),
    val selectedBeneficiarioId: String? = null,
    val errorMessage: String? = null,
    val schedulingSuccess: Boolean = false // Novo estado para sinalizar sucesso
)

class AgendarEntregaViewModel(
    private val repository: AgendarEntregaRepository,
    private val stockRepository: StockRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgendarEntregaUiState())
    val uiState: StateFlow<AgendarEntregaUiState> = _uiState.asStateFlow()

    init {
        fetchData()
    }

    private fun fetchData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val beneficiariosResponse = repository.getBeneficiarios()
                val lotesResponse = stockRepository.getAllLotes()

                if (beneficiariosResponse.success && lotesResponse.success) {
                    val activeBeneficiarios = beneficiariosResponse.data?.filter { it.estado == "ativo" } ?: emptyList()
                    _uiState.update { 
                        it.copy(
                            beneficiarios = activeBeneficiarios,
                            lotesDisponiveis = lotesResponse.data ?: emptyList()
                        )
                    }
                } else {
                     _uiState.update { it.copy(errorMessage = beneficiariosResponse.message ?: lotesResponse.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Falha de rede ao buscar dados.") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onBeneficiarioSelected(selection: String) {
        val id = _uiState.value.beneficiarios.find { b -> "${b.nomeCompleto} (${b.numEstudante ?: "N/A"})" == selection }?.id
        _uiState.update { it.copy(selectedBeneficiarioId = id, errorMessage = null) }
    }

    fun agendarEntrega(colaboradorId: String, dataAgendamentoStr: String) {
        val state = _uiState.value
        if (state.isScheduling) return
        if (state.selectedBeneficiarioId == null) {
            _uiState.update { it.copy(errorMessage = "Selecione um beneficiário.") }
            return
        }
        if (state.itensSelecionados.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Selecione pelo menos um item.") }
            return
        }

        val dataFormatada = try {
            java.time.LocalDate.parse(dataAgendamentoStr, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                .format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (e: Exception) {
            _uiState.update { it.copy(errorMessage = "Formato de data inválido.") }
            return
        }

        val itens = state.itensSelecionados.map { AgendarEntregaItemRequest(it.lote.id, it.quantidade) }
        val request = AgendarEntregaRequest(state.selectedBeneficiarioId, colaboradorId, dataFormatada, itens)

        viewModelScope.launch {
            _uiState.update { it.copy(isScheduling = true, errorMessage = null) }
            try {
                val response = repository.agendarEntrega(request)
                if (response.success) {
                    _uiState.update { it.copy(schedulingSuccess = true) } // SINALIZA SUCESSO
                } else {
                    _uiState.update { it.copy(errorMessage = response.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Falha de ligação: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isScheduling = false) }
            }
        }
    }

    fun onNavigationHandled() {
        _uiState.update { it.copy(schedulingSuccess = false) }
    }

    fun adicionarItem(lote: LoteIndividual, quantidade: Int) {
        if (quantidade <= 0 || quantidade > lote.quantidadeAtual) {
            _uiState.update { it.copy(errorMessage = "Quantidade inválida. Máximo: ${lote.quantidadeAtual}") }
            return
        }
        val item = ItemSelecionado(lote, quantidade)
        _uiState.update { s -> s.copy(itensSelecionados = s.itensSelecionados + item, errorMessage = null) }
    }

    fun removerItem(loteId: String) {
        _uiState.update { s -> s.copy(itensSelecionados = s.itensSelecionados.filter { it.lote.id != loteId }) }
    }

    fun atualizarQuantidade(loteId: String, novaQuantidade: Int) {
        _uiState.update { state ->
            val item = state.itensSelecionados.find { it.lote.id == loteId }
            if (item != null && novaQuantidade > 0 && novaQuantidade <= item.lote.quantidadeAtual) {
                state.copy(itensSelecionados = state.itensSelecionados.map { if (it.lote.id == loteId) it.copy(quantidade = novaQuantidade) else it })
            } else {
                state
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}