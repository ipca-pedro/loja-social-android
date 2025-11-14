package com.example.loja_social

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import org.json.JSONObject

/**
 * Esta classe gere a sessão do utilizador.
 * Guarda o token JWT e extrai o ID do colaborador a partir do próprio token,
 * guardando ambos de forma persistente.
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "LojaSocialPrefs"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_COLABORADOR_ID = "colaborador_id"
    }

    /**
     * Guarda o token JWT e extrai o ID do colaborador a partir dele.
     * @param token O token JWT recebido da API.
     */
    fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString(KEY_AUTH_TOKEN, token)

        // Descodifica o token para extrair o ID do colaborador
        try {
            val payload = decodeJwtPayload(token)
            val colaboradorId = payload.optString("id", null)
            if (colaboradorId != null) {
                editor.putString(KEY_COLABORADOR_ID, colaboradorId)
            }
        } catch (e: Exception) {
            // Se a descodificação falhar, limpa o ID antigo por segurança
            editor.remove(KEY_COLABORADOR_ID)
        }
        editor.apply()
    }

    /**
     * Obtém o token JWT guardado.
     */
    fun fetchAuthToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }

    /**
     * Obtém o ID do colaborador guardado.
     */
    fun fetchColaboradorId(): String? {
        return prefs.getString(KEY_COLABORADOR_ID, null)
    }

    /**
     * Apaga todos os dados da sessão (usado para o Logout).
     */
    fun clearAuthToken() {
        val editor = prefs.edit()
        editor.remove(KEY_AUTH_TOKEN)
        editor.remove(KEY_COLABORADOR_ID)
        editor.apply()
    }

    /**
     * Descodifica o payload de um token JWT.
     * Nota: Isto NÃO valida a assinatura do token. Apenas descodifica o conteúdo.
     * @param token O token JWT.
     * @return Um JSONObject com o payload do token.
     */
    private fun decodeJwtPayload(token: String): JSONObject {
        val parts = token.split(".")
        if (parts.size < 2) throw IllegalArgumentException("Invalid JWT token")
        val payloadBytes = Base64.decode(parts[1], Base64.URL_SAFE)
        return JSONObject(String(payloadBytes, Charsets.UTF_8))
    }
}