package com.example.loja_social.repository

import com.example.loja_social.api.ApiService
import com.example.loja_social.api.BeneficiariosResponse
import com.example.loja_social.api.BeneficiarioRequest
import com.example.loja_social.api.FullSingleBeneficiarioResponse // <-- Import necessário para resolver o erro
import com.example.loja_social.api.SingleBeneficiarioResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class BeneficiarioRepository(private val apiService: ApiService) {

    // Lista todos os beneficiários
    suspend fun getBeneficiarios(): BeneficiariosResponse {
        return withContext(Dispatchers.IO) {
            apiService.getBeneficiarios()
        }
    }

    /**
     * [NOVO] Obtém um único beneficiário (simulado usando a listagem completa, pois não há endpoint específico).
     */
    suspend fun getBeneficiario(beneficiarioId: String): FullSingleBeneficiarioResponse {
        return withContext(Dispatchers.IO) {
            // Primeiro, buscamos a lista completa (simulando a busca de um único item)
            val response = apiService.getBeneficiarios()

            if (response.success && response.data.isNotEmpty()) {
                // Filtramos a lista para encontrar o beneficiário com o ID
                val beneficiario = response.data.find { it.id == beneficiarioId }
                if (beneficiario != null) {
                    FullSingleBeneficiarioResponse(true, "Beneficiário carregado com sucesso.", beneficiario)
                } else {
                    FullSingleBeneficiarioResponse(false, "Beneficiário com ID '$beneficiarioId' não encontrado.", null)
                }
            } else {
                // Erro ao carregar lista (ou lista vazia)
                FullSingleBeneficiarioResponse(
                    response.success,
                    response.message ?: "Falha ao carregar lista de beneficiários da API.",
                    null
                )
            }
        }
    }

    // [NOVO] Cria um novo beneficiário (POST)
    suspend fun createBeneficiario(request: BeneficiarioRequest): SingleBeneficiarioResponse {
        return withContext(Dispatchers.IO) {
            try {
                apiService.createBeneficiario(request)
            } catch (e: HttpException) {
                // Tratar erros HTTP específicos
                android.util.Log.e("BeneficiarioRepository", "Erro HTTP ao criar beneficiário: ${e.code()}", e)
                val errorBody = e.response()?.errorBody()?.string()
                android.util.Log.d("BeneficiarioRepository", "Error body: $errorBody")
                
                when (e.code()) {
                    409 -> SingleBeneficiarioResponse(
                        false,
                        "Já existe um registo com estes dados (email, número de estudante ou NIF).",
                        null
                    )
                    400 -> SingleBeneficiarioResponse(
                        false,
                        "Dados inválidos. Verifique os campos preenchidos.",
                        null
                    )
                    500 -> SingleBeneficiarioResponse(
                        false,
                        "Erro no servidor. Verifique se o email, número de estudante ou NIF já estão registados.",
                        null
                    )
                    else -> SingleBeneficiarioResponse(
                        false,
                        "Erro ao criar beneficiário: ${e.message()}",
                        null
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("BeneficiarioRepository", "Erro ao criar beneficiário", e)
                SingleBeneficiarioResponse(
                    false,
                    "Falha de ligação: ${e.message ?: "Erro desconhecido"}",
                    null
                )
            }
        }
    }

    // [NOVO] Atualiza um beneficiário existente (PUT)
    suspend fun updateBeneficiario(beneficiarioId: String, request: BeneficiarioRequest): SingleBeneficiarioResponse {
        return withContext(Dispatchers.IO) {
            apiService.updateBeneficiario(beneficiarioId, request)
        }
    }
}