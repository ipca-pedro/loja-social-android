package com.example.loja_social.ui.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loja_social.api.AlertaValidade
import com.example.loja_social.api.Entrega
import com.example.loja_social.api.Produto
import com.example.loja_social.repository.DashboardRepository
import com.example.loja_social.repository.StockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class DashboardTab(val title: String) {
    ENTREGAS("ENTREGAS AGENDADAS"),
    ALERTAS("ALERTAS DE VALIDADE")
}

data class DashboardUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,

    // Alertas categorizados por urgência
    val alertasExpirados: List<AlertaValidade> = emptyList(),
    val alertasCriticos: List<AlertaValidade> = emptyList(),
    val alertasAtencao: List<AlertaValidade> = emptyList(),
    val alertasBrevemente: List<AlertaValidade> = emptyList(),
    
    // Lista de produtos para encontrar IDs
    val produtos: List<Produto> = emptyList(),

    val entregasAgendadas: List<Entrega> = emptyList(),
    val datasComEntregas: Set<LocalDate> = emptySet(),
    val selectedDate: LocalDate = LocalDate.now(),
    val entregasDoDiaSelecionado: List<Entrega> = emptyList(),
    val selectedTab: DashboardTab = DashboardTab.ENTREGAS
)

class DashboardViewModel(
    private val dashboardRepository: DashboardRepository,
    private val stockRepository: StockRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        fetchDashboardData()
    }

    fun fetchDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // buscar tudo em paralelo
                val alertasResponse = dashboardRepository.getAlertasValidade()
                val entregasResponse = dashboardRepository.getEntregas()
                val produtosResponse = stockRepository.getProdutos()


                if (alertasResponse.success && entregasResponse.success && produtosResponse.success) {
                    val scheduledDeliveries = entregasResponse.data.filter { it.estado.equals("agendada", ignoreCase = true) }
                    val deliveryDates = scheduledDeliveries.mapNotNull { 
                        try {
                            LocalDate.parse(it.dataAgendamento.substringBefore('T')) 
                        } catch(e: Exception) {
                            null
                        }
                    }.toSet()
                    
                    // Categoriza os alertas por urgência
                    val allAlerts = alertasResponse.data.sortedBy { it.diasRestantes }
                    val expired = allAlerts.filter { it.diasRestantes < 0 }
                    val critical = allAlerts.filter { it.diasRestantes in 0..7 }
                    val attention = allAlerts.filter { it.diasRestantes in 8..14 }
                    val soon = allAlerts.filter { it.diasRestantes in 15..30 }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            alertasExpirados = expired,
                            alertasCriticos = critical,
                            alertasAtencao = attention,
                            alertasBrevemente = soon,
                            produtos = produtosResponse.data, // guardar lista de produtos
                            entregasAgendadas = scheduledDeliveries,
                            datasComEntregas = deliveryDates
                        )
                    }
                    
                    selectDate(_uiState.value.selectedDate)
                } else {
                    val errorMsg = alertasResponse.message ?: entregasResponse.message ?: produtosResponse.message ?: "Erro desconhecido"
                    _uiState.update { it.copy(isLoading = false, errorMessage = errorMsg) }
                }

            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Falha de rede", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "Falha na ligação: ${e.message}") }
            }
        }
    }

    fun getProductIdFromAlert(alerta: AlertaValidade): Int? {
        return uiState.value.produtos.find { it.nome == alerta.produto }?.id
    }

    fun selectDate(date: LocalDate) {
        val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val deliveriesForDay = _uiState.value.entregasAgendadas.filter {
            it.dataAgendamento.startsWith(dateString)
        }
        _uiState.update {
            it.copy(
                selectedDate = date,
                entregasDoDiaSelecionado = deliveriesForDay
            )
        }
    }

    fun selectTab(tab: DashboardTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun refresh() {
        fetchDashboardData()
    }
}