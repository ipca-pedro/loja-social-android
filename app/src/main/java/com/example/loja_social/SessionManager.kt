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
        private const val KEY_USER_ROLE = "user_role"
    }

    /**
     * Guarda o token JWT e extrai o ID do colaborador a partir dele.
     * @param token O token JWT recebido da API.
     * @param role O role do utilizador ("admin" ou "beneficiario").
     */
    fun saveAuthToken(token: String, role: String? = null) {
        android.util.Log.d("SessionManager", "Guardando token e role: '$role'")
        val editor = prefs.edit()
        editor.putString(KEY_AUTH_TOKEN, token)
        
        if (role != null) {
            editor.putString(KEY_USER_ROLE, role)
            android.util.Log.d("SessionManager", "Role '$role' guardado com sucesso")
        } else {
            android.util.Log.w("SessionManager", "Role é null, não foi guardado")
        }

        // Descodifica o token para extrair o ID do colaborador
        try {
            val payload = decodeJwtPayload(token)
            val colaboradorId = payload.optString("id")
            if (colaboradorId.isNotEmpty()) {
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
     * Obtém o role do utilizador guardado.
     */
    fun fetchUserRole(): String? {
        val role = prefs.getString(KEY_USER_ROLE, null)
        android.util.Log.d("SessionManager", "Role recuperado: '$role'")
        return role
    }
    
    /**
     * Verifica se o utilizador é administrador.
     */
    fun isAdmin(): Boolean {
        return fetchUserRole() == "admin"
    }
    
    /**
     * Verifica se o utilizador é beneficiário.
     */
    fun isBeneficiario(): Boolean {
        return fetchUserRole() == "beneficiario"
    }

    /**
     * Apaga todos os dados da sessão (usado para o Logout).
     */
    fun clearAuthToken() {
        val editor = prefs.edit()
        editor.remove(KEY_AUTH_TOKEN)
        editor.remove(KEY_COLABORADOR_ID)
        editor.remove(KEY_USER_ROLE)
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