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

        // Ver se o URL do pedido começa com o caminho de admin
        val isAdminRoute = originalRequest.url.encodedPath.startsWith("/api/admin/")
        val isAuthRoute = originalRequest.url.encodedPath.startsWith("/api/auth/")

        // Se NÃO for uma rota de admin, deixa passar sem token
        if (!isAdminRoute || isAuthRoute) {
            return chain.proceed(originalRequest)
        }

        // Se for uma rota de admin, ir buscar o token ao "cofre"
        val token = sessionManager.fetchAuthToken()

        // Se não tivermos token, deixamos seguir (a API vai rejeitar com 401)
        if (token == null) {
            Log.w("AuthInterceptor", "Tentativa de aceder rota admin sem token: ${originalRequest.url}")
            return chain.proceed(originalRequest)
        }

        // Adicionar o Header "Authorization" ao pedido
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