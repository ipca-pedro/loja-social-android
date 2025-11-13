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

/**
 * Representa um item de stock selecionado para entrega.
 * @param lote O lote de stock selecionado
 * @param quantidade A quantidade a entregar deste lote (deve ser <= quantidadeAtual do lote)
 */
data class ItemSelecionado(
    val lote: LoteIndividual,
    val quantidade: Int
)

/**
 * Estado da UI do formulário de agendamento de entrega.
 * @param isLoading Indica se está a carregar dados iniciais (beneficiários e lotes)
 * @param isScheduling Indica se está a processar o agendamento
 * @param beneficiarios Lista de beneficiários disponíveis para seleção
 * @param lotesDisponiveis Lista de lotes de stock disponíveis para seleção
 * @param itensSelecionados Lista de itens já selecionados pelo utilizador
 * @param errorMessage Mensagem de erro a exibir (null se não houver erro)
 * @param successMessage Mensagem de sucesso a exibir (null se não houver sucesso)
 */
data class AgendarEntregaUiState(
    val isLoading: Boolean = true,
    val isScheduling: Boolean = false,
    val beneficiarios: List<Beneficiario> = emptyList(),
    val lotesDisponiveis: List<LoteIndividual> = emptyList(),
    val itensSelecionados: List<ItemSelecionado> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null
)

/**
 * ViewModel para o fluxo de agendamento de entregas.
 * Gerencia o estado do formulário, busca beneficiários e lotes disponíveis,
 * e processa o agendamento de entregas com múltiplos itens de stock.
 */
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

    /**
     * Busca a lista de beneficiários disponíveis para seleção no formulário.
     * Atualiza o estado com os beneficiários ou uma mensagem de erro em caso de falha.
     */
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

    /**
     * Busca todos os lotes de stock disponíveis para seleção.
     * Os lotes são filtrados no servidor para mostrar apenas os com quantidade > 0.
     * Erros são logados mas não interrompem o fluxo (o utilizador pode continuar sem lotes).
     */
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

    /**
     * Adiciona um item à lista de itens selecionados para entrega.
     * Valida que a quantidade é positiva e não excede a quantidade disponível no lote.
     * @param lote O lote de stock a adicionar
     * @param quantidade A quantidade a entregar (deve ser > 0 e <= quantidadeAtual)
     */
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

    /**
     * Remove um item da lista de itens selecionados.
     * @param loteId O ID do lote a remover
     */
    fun removerItem(loteId: String) {
        _uiState.update { state ->
            state.copy(
                itensSelecionados = state.itensSelecionados.filter { it.lote.id != loteId }
            )
        }
    }

    /**
     * Atualiza a quantidade de um item já selecionado.
     * Valida que a nova quantidade é válida antes de atualizar.
     * @param loteId O ID do lote a atualizar
     * @param novaQuantidade A nova quantidade (deve ser > 0 e <= quantidadeAtual)
     */
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
     * Processa o agendamento de uma entrega.
     * Valida os dados, formata a data, mapeia os itens selecionados e envia a requisição à API.
     * 
     * @param beneficiarioId O ID (UUID) do beneficiário selecionado
     * @param dataAgendamentoStr A data no formato DD/MM/AAAA (será convertida para ISO_LOCAL_DATE)
     * @throws IllegalStateException Se não houver itens selecionados ou se a data for inválida
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

    /**
     * Limpa as mensagens de erro e sucesso do estado.
     * Útil para resetar o feedback visual após o utilizador interagir com a UI.
     */
    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}