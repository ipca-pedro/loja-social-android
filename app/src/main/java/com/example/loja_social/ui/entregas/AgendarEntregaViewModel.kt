package com.example.loja_social.ui.entregas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loja_social.api.AgendarEntregaItemRequest
import com.example.loja_social.api.AgendarEntregaRequest
import com.example.loja_social.api.Beneficiario
import com.example.loja_social.api.LoteIndividual
import com.example.loja_social.repository.AgendarEntregaRepository
import com.example.loja_social.repository.StockRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Eventos unidirecionais do ViewModel para o Fragment.
 * Usado para comunicação de eventos únicos (como mensagens de sucesso).
 */
sealed class AgendarEntregaEvent {
    /**
     * Evento para exibir uma mensagem de sucesso.
     * @param message Mensagem a exibir
     */
    data class ShowSuccessMessage(val message: String) : AgendarEntregaEvent()
}

/**
 * Representa um item selecionado para a entrega.
 * @param lote O lote de stock selecionado
 * @param quantidade A quantidade a entregar deste lote
 */
data class ItemSelecionado(
    val lote: LoteIndividual,
    val quantidade: Int
)

/**
 * Estado da UI do ecrã de agendamento de entrega.
 * @param isLoading Indica se os dados estão a ser carregados
 * @param isScheduling Indica se a entrega está a ser agendada
 * @param beneficiarios Lista de beneficiários disponíveis
 * @param lotesDisponiveis Lista de lotes de stock disponíveis
 * @param itensSelecionados Lista de itens selecionados para a entrega
 * @param selectedBeneficiarioId ID do beneficiário selecionado
 * @param errorMessage Mensagem de erro, se houver
 */
data class AgendarEntregaUiState(
    val isLoading: Boolean = false,
    val isScheduling: Boolean = false,
    val beneficiarios: List<Beneficiario> = emptyList(),
    val lotesDisponiveis: List<LoteIndividual> = emptyList(),
    val itensSelecionados: List<ItemSelecionado> = emptyList(),
    val selectedBeneficiarioId: String? = null, 
    val errorMessage: String? = null
)

/**
 * ViewModel para o ecrã de agendamento de entregas.
 * Gerencia o estado da UI, carrega dados necessários (beneficiários e lotes),
 * e processa o agendamento de entregas.
 * 
 * @param repository Repository para operações de agendamento de entregas
 * @param stockRepository Repository para operações de stock
 */
class AgendarEntregaViewModel(
    private val repository: AgendarEntregaRepository,
    private val stockRepository: StockRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgendarEntregaUiState())
    val uiState: StateFlow<AgendarEntregaUiState> = _uiState.asStateFlow()

    private val _eventChannel = Channel<AgendarEntregaEvent>()
    val events = _eventChannel.receiveAsFlow()

    private var fetchDataJob: Job? = null

    /**
     * Inicializa o ViewModel quando o Fragment está pronto.
     * Carrega beneficiários e lotes disponíveis em paralelo.
     */
    fun onFragmentReady() {
        fetchDataJob?.cancel()
        _uiState.value = AgendarEntregaUiState()
        fetchDataJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                launch { fetchBeneficiarios() }.join()
                launch { fetchLotesDisponiveis() }.join()
            } finally {
                // Verifica se a coroutine ainda está ativa antes de atualizar o estado
                // Evita atualizar o estado após cancelamento
                if (isActive) {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    /**
     * Processa a seleção de um beneficiário no dropdown.
     * @param selection String formatada do beneficiário selecionado (formato: "Nome (Número Estudante)")
     */
    fun onBeneficiarioSelected(selection: String) {
        val id = _uiState.value.beneficiarios.find { b -> "${b.nomeCompleto} (${b.numEstudante ?: "N/A"})" == selection }?.id
        _uiState.update { it.copy(selectedBeneficiarioId = id, errorMessage = null) }
    }

    /**
     * Carrega a lista de beneficiários da API.
     */
    private suspend fun fetchBeneficiarios() {
        try {
            val response = repository.getBeneficiarios()
            if (response.success) {
                _uiState.update { it.copy(beneficiarios = response.data ?: emptyList()) }
            } else {
                _uiState.update { it.copy(errorMessage = response.message) }
            }
        } catch (e: Exception) {
            if (e !is kotlinx.coroutines.CancellationException) {
                _uiState.update { it.copy(errorMessage = "Falha de rede ao buscar beneficiários.") }
            }
        }
    }

    /**
     * Carrega a lista de lotes de stock disponíveis da API.
     */
    private suspend fun fetchLotesDisponiveis() {
        try {
            val response = stockRepository.getAllLotes()
            if (response.success) {
                _uiState.update { it.copy(lotesDisponiveis = response.data ?: emptyList()) }
            } else {
                _uiState.update { it.copy(errorMessage = response.message) }
            }
        } catch (e: Exception) {
            if (e !is kotlinx.coroutines.CancellationException) {
                _uiState.update { it.copy(errorMessage = "Falha de rede ao buscar lotes.") }
            }
        }
    }

    /**
     * Agenda uma nova entrega.
     * Valida os dados e envia a requisição para a API.
     * 
     * @param colaboradorId ID do colaborador que está a agendar a entrega
     * @param dataAgendamentoStr Data de agendamento no formato DD/MM/YYYY
     */
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
                    onFragmentReady()
                    _eventChannel.send(AgendarEntregaEvent.ShowSuccessMessage("Entrega agendada com sucesso!"))
                } else {
                    _uiState.update { it.copy(isScheduling = false, errorMessage = response.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isScheduling = false, errorMessage = "Falha de ligação: ${e.message}") }
            }
        }
    }

    /**
     * Adiciona um item à lista de itens selecionados para a entrega.
     * Valida se a quantidade é válida antes de adicionar.
     * 
     * @param lote O lote de stock a adicionar
     * @param quantidade A quantidade a entregar
     */
    fun adicionarItem(lote: LoteIndividual, quantidade: Int) {
        if (quantidade <= 0 || quantidade > lote.quantidadeAtual) {
            _uiState.update { it.copy(errorMessage = "Quantidade inválida. Máximo: ${lote.quantidadeAtual}") }
            return
        }
        val item = ItemSelecionado(lote, quantidade)
        _uiState.update { s -> s.copy(itensSelecionados = s.itensSelecionados + item, errorMessage = null) }
    }

    /**
     * Remove um item da lista de itens selecionados.
     * @param loteId ID do lote a remover
     */
    fun removerItem(loteId: String) {
        _uiState.update { s -> s.copy(itensSelecionados = s.itensSelecionados.filter { it.lote.id != loteId }) }
    }

    /**
     * Atualiza a quantidade de um item já selecionado.
     * Valida se a nova quantidade é válida antes de atualizar.
     * 
     * @param loteId ID do lote cuja quantidade será atualizada
     * @param novaQuantidade Nova quantidade a definir
     */
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

    /**
     * Limpa as mensagens de erro do estado.
     */
    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
