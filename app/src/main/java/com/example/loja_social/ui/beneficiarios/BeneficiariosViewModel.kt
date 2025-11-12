package com.example.loja_social.ui.main // O package será corrigido no Passo 3

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loja_social.api.Beneficiario // Importar o modelo completo
import com.example.loja_social.repository.BeneficiarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// NOME DA CLASSE MUDADO
class BeneficiariosViewModel(
    private val repository: BeneficiarioRepository
) : ViewModel() {

    // ESTES SÃO OS ESTADOS QUE O SEU FRAGMENTO PROCURA
    private val _uiState = MutableStateFlow<List<Beneficiario>>(emptyList())
    val uiState: StateFlow<List<Beneficiario>> = _uiState

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        fetchBeneficiarios()
    }

    private fun fetchBeneficiarios() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = repository.getBeneficiarios()
                if (response.success) {
                    _uiState.value = response.data
                    Log.d("BeneficiariosVM", "Carregados ${response.data.size} beneficiários.")
                } else {
                    Log.w("BeneficiariosVM", "API retornou erro: ${response.message}")
                    _errorMessage.value = response.message ?: "Erro desconhecido da API"
                }
            } catch (e: Exception) {
                Log.e("BeneficiariosVM", "Falha de rede", e)
                _errorMessage.value = "Falha na ligação: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}