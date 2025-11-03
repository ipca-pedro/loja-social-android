package com.example.loja_social

import android.content.Context
import android.content.SharedPreferences

/**
 * Esta classe gere a sessão do utilizador (o token JWT).
 * Usa SharedPreferences para guardar o token de forma persistente.
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences
    private val editor: SharedPreferences.Editor

    companion object {
        private const val PREFS_NAME = "LojaSocialPrefs"
        private const val KEY_AUTH_TOKEN = "auth_token"
    }

    init {
        // Inicializa as SharedPreferences
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        editor = prefs.edit()
    }

    /**
     * Guarda o token JWT nas SharedPreferences.
     */
    fun saveAuthToken(token: String) {
        editor.putString(KEY_AUTH_TOKEN, token)
        editor.apply() // Salva de forma assíncrona
    }

    /**
     * Obtém o token JWT guardado.
     * Retorna null se não existir nenhum token.
     */
    fun fetchAuthToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }

    /**
     * Apaga o token (usado para o Logout).
     */
    fun clearAuthToken() {
        editor.remove(KEY_AUTH_TOKEN)
        editor.apply()
    }
}