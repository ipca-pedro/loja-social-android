package com.example.loja_social.ui.beneficiarios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loja_social.api.Beneficiario
import com.example.loja_social.api.BeneficiarioRequest
import com.example.loja_social.repository.BeneficiarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.lang.Exception

// Estado que a UI precisa para carregar o formulário e gerir feedback
data class DetailUiState(
    val isLoading: Boolean = true, // A carregar dados do beneficiário (só no modo de edição)
    val isSaving: Boolean = false, // A enviar o formulário
    val beneficiario: Beneficiario? = null, // O objeto Beneficiario
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class BeneficiarioDetailViewModel(
    private val repository: BeneficiarioRepository,
    private val beneficiariosList: List<Beneficiario>, // Lista em cache do fragmento anterior
    private val beneficiarioId: String? // null para criar novo, ID para editar
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState

    init {
        loadBeneficiario()
    }

    /**
     * Carrega os dados se estivermos em modo de edição (ID não nulo).
     * Usa a lista do ecrã anterior como cache para evitar uma nova chamada de API.
     */
    private fun loadBeneficiario() {
        if (beneficiarioId == null) {
            // Modo de criação
            _uiState.value = DetailUiState(isLoading = false, beneficiario = null)
            return
        }

        // Modo de edição: procura o beneficiário na lista em cache
        val beneficiario = beneficiariosList.find { it.id == beneficiarioId }

        if (beneficiario != null) {
            _uiState.value = DetailUiState(isLoading = false, beneficiario = beneficiario)
        } else {
            // Caso o beneficiário não estivesse na cache (o que não deve acontecer)
            _uiState.value = DetailUiState(
                isLoading = false,
                errorMessage = "Erro: Beneficiário não encontrado na cache local.",
                beneficiario = null
            )
        }
    }

    /**
     * RF2: Envia os dados para a API (POST para criar, PUT para editar).
     */
    fun saveBeneficiario(request: BeneficiarioRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null, successMessage = null)

            try {
                val response = if (beneficiarioId == null) {
                    // Modo de Criação (POST)
                    repository.createBeneficiario(request)
                } else {
                    // Modo de Edição (PUT)
                    repository.updateBeneficiario(beneficiarioId, request)
                }

                if (response.success) {
                    val message = if (beneficiarioId == null) "Beneficiário criado com sucesso!" else "Beneficiário atualizado com sucesso!"
                    _uiState.value = _uiState.value.copy(isSaving = false, successMessage = message)
                    // Nota: O ecrã de lista precisa de ser recarregado (será feito no onResume do Fragmento de lista)
                } else {
                    val errorMsg = response.message ?: "Erro desconhecido ao guardar dados."
                    _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = "Falha de rede: ${e.message}")
            }
        }
    }
}