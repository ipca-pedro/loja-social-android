package com.example.loja_social.repository

import com.example.loja_social.api.ApiService
import com.example.loja_social.api.BeneficiariosResponse
import com.example.loja_social.api.BeneficiarioRequest
import com.example.loja_social.api.FullSingleBeneficiarioResponse
import com.example.loja_social.api.SingleBeneficiarioResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

/**
 * Repository para operações relacionadas com beneficiários.
 * Centraliza todas as chamadas à API de gestão de beneficiários (CRUD).
 */
class BeneficiarioRepository(private val apiService: ApiService) {

    /**
     * Obtém a lista de todos os beneficiários.
     * @return Resposta da API com a lista de beneficiários
     */
    suspend fun getBeneficiarios(): BeneficiariosResponse {
        return withContext(Dispatchers.IO) {
            apiService.getBeneficiarios()
        }
    }

    /**
     * Obtém um único beneficiário pelo seu ID.
     * Nota: Como não existe endpoint específico, busca a lista completa e filtra localmente.
     * @param beneficiarioId O ID (UUID) do beneficiário
     * @return Resposta da API com o beneficiário encontrado ou null se não encontrado
     */
    suspend fun getBeneficiario(beneficiarioId: String): FullSingleBeneficiarioResponse {
        return withContext(Dispatchers.IO) {
            // Busca a lista completa e filtra pelo ID
            val response = apiService.getBeneficiarios()

            if (response.success && response.data.isNotEmpty()) {
                val beneficiario = response.data.find { it.id == beneficiarioId }
                if (beneficiario != null) {
                    FullSingleBeneficiarioResponse(true, "Beneficiário carregado com sucesso.", beneficiario)
                } else {
                    FullSingleBeneficiarioResponse(false, "Beneficiário com ID '$beneficiarioId' não encontrado.", null)
                }
            } else {
                FullSingleBeneficiarioResponse(
                    response.success,
                    response.message ?: "Falha ao carregar lista de beneficiários da API.",
                    null
                )
            }
        }
    }

    /**
     * Cria um novo beneficiário.
     * Trata erros HTTP específicos (409 Conflict para duplicados, 400 Bad Request, etc.).
     * @param request Dados do novo beneficiário
     * @return Resposta da API com o beneficiário criado ou mensagem de erro
     */
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

    /**
     * Atualiza um beneficiário existente.
     * @param beneficiarioId O ID (UUID) do beneficiário a atualizar
     * @param request Dados atualizados do beneficiário
     * @return Resposta da API com o beneficiário atualizado ou mensagem de erro
     */
    suspend fun updateBeneficiario(beneficiarioId: String, request: BeneficiarioRequest): SingleBeneficiarioResponse {
        return withContext(Dispatchers.IO) {
            apiService.updateBeneficiario(beneficiarioId, request)
        }
    }
}