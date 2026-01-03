package com.example.loja_social.repository

import com.example.loja_social.api.ApiService
import com.example.loja_social.api.CampanhaRequest
import com.example.loja_social.api.Campanha
import retrofit2.HttpException
import java.io.IOException

class CampanhaRepository(private val apiService: ApiService) {

    suspend fun getCampanhas(): Result<List<Campanha>> {
        return try {
            val response = apiService.getCampanhas()
            if (response.success) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Erro desconhecido ao carregar campanhas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createCampanha(nome: String, descricao: String, dataInicio: String, dataFim: String): Result<Campanha> {
        return try {
            val request = CampanhaRequest(nome, descricao, dataInicio, dataFim, true)
            val response = apiService.createCampanha(request)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Erro ao criar campanha"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCampanha(id: String, nome: String, descricao: String, dataInicio: String, dataFim: String, ativo: Boolean): Result<Campanha> {
        return try {
            val request = CampanhaRequest(nome, descricao, dataInicio, dataFim, ativo)
            val response = apiService.updateCampanha(id, request)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Erro ao atualizar campanha"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCampanha(id: String): Result<Boolean> {
        return try {
            val response = apiService.deleteCampanha(id)
            if (response.success) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.message ?: "Erro ao remover campanha"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
