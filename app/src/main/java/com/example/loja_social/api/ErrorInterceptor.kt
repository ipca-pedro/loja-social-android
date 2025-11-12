package com.example.loja_social.api

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONObject

/**
 * Interceptor para tratar erros HTTP e padronizar respostas de erro.
 * Converte erros HTTP (400, 404, 500, etc.) em respostas JSON padronizadas.
 */
class ErrorInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        // Se a resposta for bem-sucedida (200-299), deixa passar
        if (response.isSuccessful) {
            return response
        }

        // Se houver erro, tentar extrair a mensagem do body
        val errorBody = response.body?.string()
        val errorMessage = try {
            if (errorBody != null) {
                val json = JSONObject(errorBody)
                json.optString("message", "Erro desconhecido")
            } else {
                "Erro ${response.code}: ${response.message}"
            }
        } catch (e: Exception) {
            "Erro ${response.code}: ${response.message}"
        }

        Log.e("ErrorInterceptor", "Erro HTTP ${response.code} em ${request.url}: $errorMessage")

        // Criar um novo body de erro padronizado
        val errorJson = JSONObject().apply {
            put("success", false)
            put("message", errorMessage)
            put("data", null)
        }

        // Retornar uma nova resposta com o body padronizado
        return response.newBuilder()
            .body(errorJson.toString().toResponseBody(response.body?.contentType()))
            .build()
    }
}

