package com.example.loja_social.ui.beneficiario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loja_social.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class BeneficiarioMainUiState(
    val isLoading: Boolean = false,
    val minhasEntregas: List<Entrega> = emptyList(), // Todas as entregas
    val campanhasAtivas: List<Campanha> = emptyList(),
    val errorMessage: String? = null,
    val selectedDate: LocalDate = LocalDate.now(),
    val datasComEntregas: Set<LocalDate> = emptySet(),
    val entregasDoDia: List<Entrega> = emptyList(), // Entregas apenas do dia selecionado
    val unreadCount: Int = 0 
)

class BeneficiarioMainViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(BeneficiarioMainUiState())
    val uiState: StateFlow<BeneficiarioMainUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                // Carregar entregas do beneficiário
                val entregasResponse = apiService.getMinhasEntregas()
                
                // Carregar campanhas ativas
                val campanhasResponse = apiService.getCampanhas()

                // Carregar notificações (para badge)
                val notificacoesResponse = apiService.getNotificacoes() // Requer que getNotificacoes exista na ApiService (já existe no NotificationRepository mas preciso acessar aqui ou injetar repo)
                // BeneficiarioMainViewModel recebe ApiService. Posso chamar direto se a rota estiver lá.
                // A rota getNotificacoes está na ApiService (Step 1489/1499).


                // Processar datas
                val entregas = entregasResponse.data
                val datas = entregas.mapNotNull { 
                    try {
                        LocalDate.parse(it.dataAgendamento.substringBefore("T"))
                    } catch (e: Exception) { null }
                }.toSet()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    minhasEntregas = entregas,
                    campanhasAtivas = campanhasResponse.data,
                    datasComEntregas = datas,
                    unreadCount = if (notificacoesResponse.success && notificacoesResponse.data != null) {
                        notificacoesResponse.data.count { !it.lida }
                    } else 0
                )
                // Filtrar para o dia atual inicialmente
                selectDate(_uiState.value.selectedDate)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erro ao carregar dados: ${e.message}"
                )
            }
        }
    }

    fun selectDate(date: LocalDate) {
        val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE) // yyyy-MM-dd
        val filtered = _uiState.value.minhasEntregas.filter { 
            it.dataAgendamento.startsWith(dateString) 
        }
        _uiState.value = _uiState.value.copy(
            selectedDate = date,
            entregasDoDia = filtered
        )
    }

    fun refresh() {
        loadData()
    }
}