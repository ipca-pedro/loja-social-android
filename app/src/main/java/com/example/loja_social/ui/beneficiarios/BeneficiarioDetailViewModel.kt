package com.example.loja_social.ui.beneficiarios

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loja_social.api.Beneficiario
import com.example.loja_social.api.BeneficiarioRequest
import com.example.loja_social.repository.BeneficiarioRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BeneficiarioDetailUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val beneficiario: Beneficiario? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class BeneficiarioDetailViewModel(
    private val repository: BeneficiarioRepository,
    private val beneficiarioId: String?
) : ViewModel() {

    private val _uiState = MutableStateFlow(BeneficiarioDetailUiState())
    val uiState = _uiState.asStateFlow()

    // Canal para eventos de navegação (ex: fechar o fragmento)
    private val _navigateBack = MutableSharedFlow<Unit>()
    val navigateBack = _navigateBack.asSharedFlow()

    init {
        loadBeneficiario()
    }

    private fun loadBeneficiario() {
        if (beneficiarioId == null) {
            _uiState.update { it.copy(isLoading = false) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = repository.getBeneficiario(beneficiarioId)
                if (response.success && response.data != null) {
                    _uiState.update { it.copy(isLoading = false, beneficiario = response.data) }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = response.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Falha ao carregar dados: ${e.message}") }
            }
        }
    }

    fun saveBeneficiario(request: BeneficiarioRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, successMessage = null) }
            val isEditing = beneficiarioId != null
            try {
                val response = if (isEditing) {
                    repository.updateBeneficiario(beneficiarioId!!, request)
                } else {
                    repository.createBeneficiario(request)
                }

                if (response.success) {
                    val message = if (isEditing) "Beneficiário atualizado!" else "Beneficiário criado!"
                    _uiState.update { it.copy(isSaving = false, successMessage = message) }
                    // Se editou com sucesso, navega para trás
                    if (isEditing) {
                        _navigateBack.emit(Unit)
                    }
                } else {
                    _uiState.update { it.copy(isSaving = false, errorMessage = response.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = "Falha de ligação: ${e.message}") }
            }
        }
    }

    fun deactivateBeneficiario() {
        if (beneficiarioId == null) return

        val currentBeneficiario = _uiState.value.beneficiario ?: return

        val request = BeneficiarioRequest(
            nomeCompleto = currentBeneficiario.nomeCompleto,
            email = currentBeneficiario.email,
            estado = "inativo", // Força a desativação
            numEstudante = currentBeneficiario.numEstudante,
            nif = currentBeneficiario.nif,
            notasAdicionais = currentBeneficiario.notasAdicionais,
            anoCurricular = currentBeneficiario.anoCurricular,
            curso = currentBeneficiario.curso,
            telefone = currentBeneficiario.telefone
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, successMessage = null) }
            try {
                val response = repository.updateBeneficiario(beneficiarioId, request)
                if (response.success) {
                    _uiState.update { it.copy(isSaving = false, successMessage = "Beneficiário desativado.") }
                    _navigateBack.emit(Unit) // Navega para trás após desativar
                } else {
                    _uiState.update { it.copy(isSaving = false, errorMessage = response.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = "Falha de ligação: ${e.message}") }
            }
        }
    }
}