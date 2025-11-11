package com.example.loja_social.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loja_social.repository.BeneficiarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * O ViewModel que vai guardar o estado e a lógica do ecrã principal,
 * incluindo a lista de beneficiários.
 */
class MainViewModel(
    private val repository: BeneficiarioRepository
) : ViewModel() {

    // O _uiState é privado e mutável (só o ViewModel pode mudar)
    // O BeneficiariosFragment espera um String, por isso começamos com "A carregar..."
    private val _uiState = MutableStateFlow<String>("A carregar beneficiários...")

    // O uiState é público e não-mutável (os Fragments só podem ler)
    val uiState: StateFlow<String> = _uiState

    // O bloco init é executado assim que o ViewModel é criado
    init {
        // Vamos buscar os beneficiários assim que a app arranca
        fetchBeneficiarios()
    }

    private fun fetchBeneficiarios() {
        // Usamos o viewModelScope para lançar a coroutine
        viewModelScope.launch {
            try {
                // Tenta buscar os dados ao repositório (que chama a API)
                val response = repository.getBeneficiarios()

                if (response.success && response.data.isNotEmpty()) {
                    // Sucesso!
                    Log.d("MainViewModel", "Beneficiários carregados: ${response.data.size} items.")

                    // O teu Fragment está à espera de UMA string,
                    // por isso vamos formatar a lista
                    val nomes = response.data.joinToString(separator = "\n") {
                        "- ${it.nomeCompleto} (${it.estado})"
                    }
                    _uiState.value = nomes

                } else {
                    // A API pode ter dado erro (ex: token inválido)
                    Log.w("MainViewModel", "API retornou erro: ${response.message}")
                    _uiState.value = "Erro ao carregar: ${response.message}"
                }

            } catch (e: Exception) {
                // Erro de rede (ex: sem internet ou API offline)
                Log.e("MainViewModel", "Falha de rede ao buscar beneficiários", e)
                _uiState.value = "Erro de ligação: ${e.message}"
            }
        }
    }
}