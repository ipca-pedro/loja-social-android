package com.example.loja_social.api

/**
 * Wrapper genérico para resultados de chamadas à API.
 * Facilita o tratamento de sucesso/erro de forma consistente.
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int? = null) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}

/**
 * Extensão para validar respostas da API e converter para ApiResult.
 * Todas as respostas da API têm um campo "success" que indica se foi bem-sucedida.
 */
fun <T> ApiResponse<T>.toResult(): ApiResult<T> {
    return if (success && data != null) {
        ApiResult.Success(data)
    } else {
        ApiResult.Error(message ?: "Erro desconhecido")
    }
}

/**
 * Extensão para validar respostas simples (sem data).
 */
fun <T> T.toResult(success: Boolean, message: String?): ApiResult<T> {
    return if (success) {
        ApiResult.Success(this)
    } else {
        ApiResult.Error(message ?: "Erro desconhecido")
    }
}

