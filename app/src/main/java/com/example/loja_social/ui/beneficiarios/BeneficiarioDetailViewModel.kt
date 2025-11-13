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

/**
 * Estado da UI do formulário de detalhes/edição de beneficiário.
 * @param isLoading Indica se está a carregar dados do beneficiário (modo edição)
 * @param isSaving Indica se está a processar o salvamento
 * @param beneficiario Dados do beneficiário (null em modo criação)
 * @param errorMessage Mensagem de erro a exibir (null se não houver erro)
 * @param successMessage Mensagem de sucesso a exibir (null se não houver sucesso)
 */
data class BeneficiarioDetailUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val beneficiario: Beneficiario? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

/**
 * ViewModel para o formulário de criar/editar beneficiário.
 * Gerencia o carregamento de dados (em modo edição) e o salvamento (criação/atualização).
 * 
 * @param repository Repository para operações de beneficiários
 * @param beneficiarioId O ID do beneficiário (null = modo criação, não-null = modo edição)
 */
class BeneficiarioDetailViewModel(
    private val repository: BeneficiarioRepository,
    private val beneficiarioId: String?
) : ViewModel() {

    private val _uiState = MutableStateFlow(BeneficiarioDetailUiState())
    val uiState = _uiState.asStateFlow()

    /** Canal para eventos de navegação (fechar o fragmento após sucesso) */
    private val _navigateBack = MutableSharedFlow<Unit>()
    val navigateBack = _navigateBack.asSharedFlow()

    init {
        loadBeneficiario()
    }

    /**
     * Carrega os dados do beneficiário se estiver em modo edição.
     * Em modo criação (beneficiarioId == null), apenas desativa o loading.
     */
    private fun loadBeneficiario() {
        if (beneficiarioId == null) {
            // Modo criação: não precisa carregar dados, apenas desativa o loading
            _uiState.update { it.copy(isLoading = false) }
            Log.d("BeneficiarioDetailVM", "Modo de criação - não precisa carregar dados")
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

    /**
     * Salva o beneficiário (cria novo ou atualiza existente).
     * Detecta automaticamente o modo (criação ou edição) baseado em beneficiarioId.
     * Em modo edição, navega para trás após sucesso.
     * 
     * @param request Dados do beneficiário a salvar
     */
    fun saveBeneficiario(request: BeneficiarioRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, successMessage = null) }
            val isEditing = beneficiarioId != null
            try {
                Log.d("BeneficiarioDetailVM", "Salvando beneficiário: isEditing=$isEditing, request=$request")
                val response = if (isEditing) {
                    Log.d("BeneficiarioDetailVM", "Atualizando beneficiário ID: $beneficiarioId")
                    repository.updateBeneficiario(beneficiarioId!!, request)
                } else {
                    Log.d("BeneficiarioDetailVM", "Criando novo beneficiário")
                    repository.createBeneficiario(request)
                }

                Log.d("BeneficiarioDetailVM", "Resposta da API: success=${response.success}, message=${response.message}, data=${response.data}")

                if (response.success) {
                    val message = if (isEditing) "Beneficiário atualizado!" else "Beneficiário criado!"
                    _uiState.update { it.copy(isSaving = false, successMessage = message) }
                    // Em modo edição, navega para trás após sucesso
                    if (isEditing) {
                        _navigateBack.emit(Unit)
                    }
                } else {
                    val errorMsg = response.message ?: "Erro desconhecido ao salvar beneficiário"
                    Log.e("BeneficiarioDetailVM", "Erro ao salvar: $errorMsg")
                    _uiState.update { it.copy(isSaving = false, errorMessage = errorMsg) }
                }
            } catch (e: Exception) {
                Log.e("BeneficiarioDetailVM", "Exceção ao salvar beneficiário", e)
                // Trata erros específicos de constraint violations (email, NIF, número de estudante duplicados)
                val errorMsg = when {
                    e.message?.contains("500", ignoreCase = true) == true -> 
                        "Erro no servidor. Verifique se o email, número de estudante ou NIF já estão registados."
                    e.message?.contains("email", ignoreCase = true) == true -> 
                        "Este email já está registado. Use outro email."
                    e.message?.contains("num_estudante", ignoreCase = true) == true -> 
                        "Este número de estudante já está registado."
                    e.message?.contains("nif", ignoreCase = true) == true -> 
                        "Este NIF já está registado."
                    e.message?.contains("UNIQUE", ignoreCase = true) == true -> 
                        "Já existe um registo com estes dados (email, número de estudante ou NIF)."
                    else -> "Falha de ligação: ${e.message ?: "Erro desconhecido"}"
                }
                _uiState.update { it.copy(isSaving = false, errorMessage = errorMsg) }
            }
        }
    }

    /**
     * Desativa um beneficiário (muda o estado para "inativo").
     * Apenas funciona em modo edição (beneficiarioId != null).
     * Após sucesso, navega para trás.
     */
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