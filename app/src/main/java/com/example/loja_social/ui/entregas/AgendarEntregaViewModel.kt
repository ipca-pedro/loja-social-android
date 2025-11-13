package com.example.loja_social.ui.entregas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loja_social.api.AgendarEntregaItemRequest
import com.example.loja_social.api.AgendarEntregaRequest
import com.example.loja_social.api.Beneficiario
import com.example.loja_social.api.LoteIndividual
import com.example.loja_social.repository.AgendarEntregaRepository
import com.example.loja_social.repository.StockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ItemSelecionado(
    val lote: LoteIndividual,
    val quantidade: Int
)

data class AgendarEntregaUiState(
    val isLoading: Boolean = true,
    val isScheduling: Boolean = false,
    val beneficiarios: List<Beneficiario> = emptyList(),
    val lotesDisponiveis: List<LoteIndividual> = emptyList(),
    val itensSelecionados: List<ItemSelecionado> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AgendarEntregaViewModel(
    private val repository: AgendarEntregaRepository,
    private val stockRepository: StockRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgendarEntregaUiState())
    val uiState: StateFlow<AgendarEntregaUiState> = _uiState.asStateFlow()

    init {
        fetchBeneficiarios()
        fetchLotesDisponiveis()
    }

    private fun fetchBeneficiarios() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = repository.getBeneficiarios()
                if (response.success) {
                    _uiState.update { it.copy(beneficiarios = response.data ?: emptyList()) }
                } else {
                    _uiState.update { it.copy(errorMessage = response.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Falha de rede ao buscar beneficiários.") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun fetchLotesDisponiveis() {
        viewModelScope.launch {
            try {
                val response = stockRepository.getAllLotes()
                if (response.success) {
                    _uiState.update { it.copy(lotesDisponiveis = response.data) }
                } else {
                    android.util.Log.e("AgendarEntregaVM", "Erro ao buscar lotes: ${response.message}")
                }
            } catch (e: Exception) {
                android.util.Log.e("AgendarEntregaVM", "Falha ao buscar lotes", e)
            }
        }
    }

    fun adicionarItem(lote: LoteIndividual, quantidade: Int) {
        if (quantidade <= 0 || quantidade > lote.quantidadeAtual) {
            _uiState.update { it.copy(errorMessage = "Quantidade inválida. Máximo: ${lote.quantidadeAtual}") }
            return
        }

        val item = ItemSelecionado(lote, quantidade)
        _uiState.update { state ->
            state.copy(
                itensSelecionados = state.itensSelecionados + item,
                errorMessage = null
            )
        }
    }

    fun removerItem(loteId: String) {
        _uiState.update { state ->
            state.copy(
                itensSelecionados = state.itensSelecionados.filter { it.lote.id != loteId }
            )
        }
    }

    fun atualizarQuantidade(loteId: String, novaQuantidade: Int) {
        _uiState.update { state ->
            val item = state.itensSelecionados.find { it.lote.id == loteId }
            if (item != null && novaQuantidade > 0 && novaQuantidade <= item.lote.quantidadeAtual) {
                state.copy(
                    itensSelecionados = state.itensSelecionados.map {
                        if (it.lote.id == loteId) it.copy(quantidade = novaQuantidade) else it
                    }
                )
            } else {
                state
            }
        }
    }

    /**
     * RF4: Envia a requisição para agendar a entrega com os itens selecionados.
     * @param beneficiarioId O ID (UUID) do beneficiário selecionado.
     * @param dataAgendamentoStr A data no formato DD/MM/AAAA.
     */
    fun agendarEntrega(beneficiarioId: String, dataAgendamentoStr: String) {
        if (_uiState.value.isScheduling) return

        // Validar que há itens selecionados
        if (_uiState.value.itensSelecionados.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Selecione pelo menos um item de stock para a entrega.") }
            return
        }

        // 1. Formatar e validar a data
        val dataFormatada = try {
            val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
            java.time.LocalDate.parse(dataAgendamentoStr, formatter).format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (e: Exception) {
            _uiState.update { it.copy(errorMessage = "Formato de data inválido. Use DD/MM/AAAA.") }
            return
        }

        // 2. Mapear itens selecionados para AgendarEntregaItemRequest
        val itens = _uiState.value.itensSelecionados.map { item ->
            AgendarEntregaItemRequest(
                stockItemId = item.lote.id,
                quantidadeEntregue = item.quantidade
            )
        }

        // 3. Criar a requisição
        val request = AgendarEntregaRequest(
            beneficiarioId = beneficiarioId,
            dataAgendamento = dataFormatada,
            itens = itens
        )

        // 4. Chamar a API e gerir o estado
        viewModelScope.launch {
            _uiState.update { it.copy(isScheduling = true, errorMessage = null, successMessage = null) }
            try {
                val response = repository.agendarEntrega(request)
                if (response.success) {
                    _uiState.update {
                        it.copy(
                            isScheduling = false,
                            successMessage = "Entrega agendada com sucesso! ID: ${response.data?.id?.substring(0, 4)}...",
                            itensSelecionados = emptyList() // Limpar após sucesso
                        )
                    }
                } else {
                    val errorMsg = response.message ?: "Erro desconhecido ao agendar entrega."
                    _uiState.update { it.copy(isScheduling = false, errorMessage = errorMsg) }
                }
            } catch (e: Exception) {
                android.util.Log.e("AgendarEntregaVM", "Erro de rede ao agendar entrega", e)
                _uiState.update { it.copy(isScheduling = false, errorMessage = "Falha de ligação: ${e.message}") }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}