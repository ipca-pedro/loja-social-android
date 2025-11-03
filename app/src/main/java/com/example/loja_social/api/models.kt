package com.example.loja_social.api

import com.google.gson.annotations.SerializedName

// --- CÓDIGO DAS CAMPANHAS (Sem alterações) ---

data class CampanhasResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: List<Campanha>
)

data class Campanha(
    @SerializedName("id")
    val id: String,
    @SerializedName("nome")
    val nome: String,
    @SerializedName("descricao")
    val descricao: String?,
    @SerializedName("data_inicio")
    val dataInicio: String,
    @SerializedName("data_fim")
    val dataFim: String
)

// --- CÓDIGO DE LOGIN (CORRIGIDO PARA ESPERAR O TOKEN) ---

data class LoginRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)

/**
 * Esta é a LoginResponse CORRIGIDA.
 * Agora espera o 'token' que a API (Node.js) está a enviar.
 */
data class LoginResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String?,

    @SerializedName("token") // <-- A MUDANÇA IMPORTANTE ESTÁ AQUI
    val token: String? // Esperamos um token (pode ser nulo se success=false)
)