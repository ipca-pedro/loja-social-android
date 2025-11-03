package com.example.loja_social.api

import com.example.loja_social.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor para adicionar o cabeçalho de autenticação ao pedido HTTP.
 *
 */
class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {

override fun intercept(chain: Interceptor.Chain): Response {
val originalRequest = chain.request()

// Ver se o URL do pedido começa com o caminho de admin
val isAdminRoute = originalRequest.url.encodedPath.startsWith("/api/admin/")

// Se NÃO for uma rota de admin, ou se for a rota de login, deixa passar
if (!isAdminRoute) {
return chain.proceed(originalRequest)
}

// Se for uma rota de admin, ir buscar o token ao "cofre"
val token = sessionManager.fetchAuthToken()

// Se não tivermos token, não podemos fazer a chamada
// (Isto não devia acontecer se a app estiver bem feita, mas é uma segurança)
if (token == null) {
// Poderíamos lançar um erro, mas por agora, deixamos seguir
// A API vai rejeitar com um 401
return chain.proceed(originalRequest)
}

// Adicionar o Header "Authorization" ao pedido
val newRequest = originalRequest.newBuilder()
.header("Authorization", "Bearer $token")
.build()

return chain.proceed(newRequest)
}
}