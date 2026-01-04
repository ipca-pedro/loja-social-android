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
    val dataAgendamento: String = "",
    val editingDeliveryId: String? = null,
    val errorMessage: String? = null,
    val schedulingSuccess: Boolean = false
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
                    val activeBeneficiarios = beneficiariosResponse.data.filter { it.estado == "ativo" }
                    _uiState.update { 
                        it.copy(
                            beneficiarios = activeBeneficiarios,
                            lotesDisponiveis = lotesResponse.data
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

    fun loadDeliveryForEdit(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, editingDeliveryId = id) }
            try {
                // 1. Fetch Header
                val headerResponse = repository.getEntrega(id)
                if (!headerResponse.success || headerResponse.data == null) {
                    _uiState.update { it.copy(errorMessage = "Erro ao carregar entrega: ${headerResponse.message}") }
                    return@launch
                }
                
                // 2. Fetch Details
                val detailsResponse = repository.getDetalhesEntrega(id)
                if (!detailsResponse.success || detailsResponse.data == null) {
                    _uiState.update { it.copy(errorMessage = "Erro ao carregar detalhes: ${detailsResponse.message}") }
                    return@launch
                }

                // 3. Format Date (YYYY-MM-DD -> dd/MM/yyyy)
                val rawDate = headerResponse.data.dataAgendamento.take(10) // Ensure yyyy-MM-dd
                val formattedDate = try {
                    val parsedDate = java.time.LocalDate.parse(rawDate, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE) // Changed to ISO_LOCAL_DATE standard parser
                    parsedDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                } catch (e: Exception) {
                    rawDate 
                }

                // 4. Map Details to ItemSelecionado
                val mappedItems = detailsResponse.data.map { detail ->
                    ItemSelecionado(
                        lote = LoteIndividual(
                            id = detail.id,
                            produto = detail.produto,
                            categoria = detail.categoria,
                            quantidadeAtual = detail.quantidadeAtual,
                            quantidadeReservada = detail.quantidadeReservada,
                            quantidadeDanificada = detail.quantidadeDanificada,
                            dataValidade = detail.dataValidade,
                            quantidadeInicial = detail.quantidadeInicial,
                            dataEntrada = detail.dataEntrada
                        ),
                        quantidade = detail.quantidadeEntregue
                    )
                }

                _uiState.update { 
                    it.copy(
                        selectedBeneficiarioId = headerResponse.data.beneficiarioId,
                        dataAgendamento = formattedDate,
                        itensSelecionados = mappedItems
                    ) 
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Erro ao carregar dados de edição: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onBeneficiarioSelected(selection: String) {
        val id = _uiState.value.beneficiarios.find { b -> "${b.nomeCompleto} (${b.numEstudante ?: "N/A"})" == selection }?.id
        _uiState.update { it.copy(selectedBeneficiarioId = id, errorMessage = null) }
    }

    fun onDataSelected(date: String) {
        _uiState.update { it.copy(dataAgendamento = date, errorMessage = null) }
    }

    fun agendarEntrega(colaboradorId: String) {
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
        if (state.dataAgendamento.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Selecione uma data.") }
            return
        }

        val dataFormatada = try {
            java.time.LocalDate.parse(state.dataAgendamento, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
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
                val response = if (state.editingDeliveryId != null) {
                    repository.editarEntrega(state.editingDeliveryId, request)
                } else {
                    repository.agendarEntrega(request)
                }

                if (response.success) {
                    _uiState.update { it.copy(schedulingSuccess = true) } 
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
        _uiState.update { it.copy(schedulingSuccess = false, itensSelecionados = emptyList(), selectedBeneficiarioId = null) }
        fetchData() // Refresh stock data
    }

    fun adicionarItem(lote: LoteIndividual, quantidade: Int) {
        val disponivel = lote.quantidadeAtual - lote.quantidadeReservada
        if (quantidade <= 0 || quantidade > disponivel) {
            _uiState.update { 
                it.copy(errorMessage = "Não é possível agendar esta quantidade. Atualmente, apenas tem $disponivel unidades disponíveis para novos agendamentos (Stock Físico Disponível: ${lote.quantidadeAtual} | Reservado: ${lote.quantidadeReservada}).") 
            }
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
            if (item != null && novaQuantidade > 0) {
                val disponivel = item.lote.quantidadeAtual - item.lote.quantidadeReservada
                if (novaQuantidade <= disponivel) {
                    state.copy(itensSelecionados = state.itensSelecionados.map { if (it.lote.id == loteId) it.copy(quantidade = novaQuantidade) else it }, errorMessage = null)
                } else {
                    state.copy(errorMessage = "Não é possível agendar esta quantidade. Atualmente, apenas tem $disponivel unidades disponíveis para novos agendamentos (Stock Físico Disponível: ${item.lote.quantidadeAtual} | Reservado: ${item.lote.quantidadeReservada}).")
                }
            } else {
                state
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}