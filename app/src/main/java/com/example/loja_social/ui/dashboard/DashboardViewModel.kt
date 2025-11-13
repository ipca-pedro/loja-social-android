package com.example.loja_social.ui.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loja_social.api.AlertaValidade
import com.example.loja_social.repository.DashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Estado da UI do dashboard.
 * @param isLoading Indica se está a carregar dados iniciais
 * @param errorMessage Mensagem de erro a exibir (null se não houver erro)
 * @param alertas Lista de alertas de validade (produtos a vencer em breve)
 * @param entregasAgendadasCount Número de entregas agendadas (não concluídas)
 */
data class DashboardUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val alertas: List<AlertaValidade> = emptyList(),
    val entregasAgendadasCount: Int = 0
)

/**
 * ViewModel para o dashboard principal.
 * Carrega alertas de validade e contagem de entregas agendadas.
 * Faz duas chamadas à API em paralelo para otimizar o carregamento.
 */
class DashboardViewModel(
    private val repository: DashboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        fetchDashboardData()
    }

    /**
     * Busca os dados do dashboard (alertas de validade e entregas).
     * Faz duas chamadas à API em paralelo e conta entregas agendadas.
     * Se qualquer uma das chamadas falhar, exibe uma mensagem de erro.
     */
    fun fetchDashboardData() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState(isLoading = true)

            try {
                // Busca alertas de validade e lista de entregas em paralelo
                val alertasResponse = repository.getAlertasValidade()
                val entregasResponse = repository.getEntregas()

                if (alertasResponse.success && entregasResponse.success) {
                    // Conta apenas entregas com estado "agendada" (não concluídas)
                    val contagemEntregas = entregasResponse.data.count {
                        it.estado.equals("agendada", ignoreCase = true)
                    }

                    _uiState.value = DashboardUiState(
                        isLoading = false,
                        alertas = alertasResponse.data,
                        entregasAgendadasCount = contagemEntregas
                    )
                } else {
                    // Se qualquer uma das chamadas falhar, exibe erro
                    val errorMsg = alertasResponse.message ?: entregasResponse.message ?: "Erro desconhecido"
                    _uiState.value = DashboardUiState(isLoading = false, errorMessage = errorMsg)
                }

            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Falha de rede", e)
                _uiState.value = DashboardUiState(isLoading = false, errorMessage = "Falha na ligação: ${e.message}")
            }
        }
    }

    /**
     * Recarrega os dados do dashboard.
     * Útil para pull-to-refresh ou atualização manual.
     */
    fun refresh() {
        fetchDashboardData()
    }
}