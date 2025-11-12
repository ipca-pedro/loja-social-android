package com.example.loja_social.api

import android.content.Context
import android.util.Log

/**
 * Helper para garantir que RetrofitInstance está inicializado antes de usar.
 */
object RetrofitHelper {
    
    /**
     * Garante que o RetrofitInstance está inicializado.
     * Retorna true se estiver inicializado, false caso contrário.
     */
    fun ensureInitialized(context: Context): Boolean {
        return try {
            // Verificar se está inicializado usando o método helper
            if (!RetrofitInstance.isInitialized()) {
                Log.w("RetrofitHelper", "RetrofitInstance não estava inicializado, inicializando agora...")
                RetrofitInstance.initialize(context.applicationContext)
            }
            true
        } catch (e: UninitializedPropertyAccessException) {
            Log.w("RetrofitHelper", "RetrofitInstance não inicializado, tentando inicializar...")
            try {
                RetrofitInstance.initialize(context.applicationContext)
                true
            } catch (e2: Exception) {
                Log.e("RetrofitHelper", "Erro ao inicializar RetrofitInstance", e2)
                false
            }
        } catch (e: Exception) {
            Log.e("RetrofitHelper", "Erro ao verificar RetrofitInstance", e)
            try {
                // Última tentativa de inicializar
                RetrofitInstance.initialize(context.applicationContext)
                true
            } catch (e2: Exception) {
                Log.e("RetrofitHelper", "Falha crítica ao inicializar RetrofitInstance", e2)
                false
            }
        }
    }
}

