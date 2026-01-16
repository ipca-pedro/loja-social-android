package com.example.loja_social.api

import android.util.Log
import com.example.loja_social.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor para adicionar o cabeçalho de autenticação ao pedido HTTP.
 * Também trata respostas 401 (Unauthorized) para limpar o token inválido.
 */
class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Verificar se é uma rota que precisa de autenticação
        // /api/auth/login é pública, mas /api/auth/change-password precisa de token!
        val path = originalRequest.url.encodedPath
        val isChangePassword = path.contains("change-password")
        val isPublicRoute = !isChangePassword && (path.startsWith("/api/public/") || path.startsWith("/api/auth/") || path == "/health")
        
        Log.d("AuthInterceptor", "Request para: $path, É pública: $isPublicRoute")
        
        // Se for uma rota pública, deixa passar sem token
        if (isPublicRoute) {
            return chain.proceed(originalRequest)
        }

        // Se for uma rota de admin, ir buscar o token ao "cofre"
        val token = sessionManager.fetchAuthToken()

        // Se não tivermos token, deixamos seguir (a API vai rejeitar com 401)
        if (token == null) {
            Log.w("AuthInterceptor", "Tentativa de aceder rota protegida sem token: ${originalRequest.url}")
            return chain.proceed(originalRequest)
        }

        // Adicionar o Header "Authorization" ao pedido
        Log.d("AuthInterceptor", "Adicionando token Bearer para: $path")
        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        val response = chain.proceed(newRequest)

        // Se recebermos 401 (Unauthorized), o token pode estar inválido/expirado
        if (response.code == 401) {
            Log.w("AuthInterceptor", "Recebido 401 - Token pode estar inválido/expirado")
            // Limpar o token inválido
            sessionManager.clearAuthToken()
            // Poderia também disparar um evento para fazer logout, mas por agora só limpamos
        }

        return response
    }
}