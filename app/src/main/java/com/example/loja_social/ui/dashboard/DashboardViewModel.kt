package com.example.loja_social.ui.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loja_social.api.AlertaValidade
import com.example.loja_social.repository.DashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// 1. Definir um Data Class para o Estado da UI
data class DashboardUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val alertas: List<AlertaValidade> = emptyList(),
    val entregasAgendadasCount: Int = 0
)

// 2. O ViewModel
class DashboardViewModel(
    private val repository: DashboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        fetchDashboardData()
    }

    fun fetchDashboardData() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState(isLoading = true) // Começa a carregar

            try {
                // Tenta buscar os dois endpoints
                val alertasResponse = repository.getAlertasValidade()
                val entregasResponse = repository.getEntregas()

                if (alertasResponse.success && entregasResponse.success) {
                    // Sucesso! Contar entregas "agendadas"
                    val contagemEntregas = entregasResponse.data.count {
                        it.estado.equals("agendada", ignoreCase = true)
                    }

                    _uiState.value = DashboardUiState(
                        isLoading = false,
                        alertas = alertasResponse.data,
                        entregasAgendadasCount = contagemEntregas
                    )
                } else {
                    // Se um deles falhar
                    val errorMsg = alertasResponse.message ?: entregasResponse.message ?: "Erro desconhecido"
                    _uiState.value = DashboardUiState(isLoading = false, errorMessage = errorMsg)
                }

            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Falha de rede", e)
                _uiState.value = DashboardUiState(isLoading = false, errorMessage = "Falha na ligação: ${e.message}")
            }
        }
    }
}