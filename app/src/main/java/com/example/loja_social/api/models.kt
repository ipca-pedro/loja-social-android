package com.example.loja_social.api

import com.google.gson.annotations.SerializedName

// --- Modelos Públicos ---

data class CampanhasResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<Campanha>
)

data class Campanha(
    @SerializedName("id") val id: String,
    @SerializedName("nome") val nome: String,
    @SerializedName("descricao") val descricao: String?,
    @SerializedName("data_inicio") val dataInicio: String,
    @SerializedName("data_fim") val dataFim: String
)

// --- Modelos de Autenticação (OS QUE FALTAVAM) ---

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("token") val token: String?
)

// --- Modelos de Admin ---

data class Beneficiario(
    @SerializedName("id") val id: String,
    @SerializedName("nome_completo") val nomeCompleto: String,
    @SerializedName("num_estudante") val numEstudante: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("estado") val estado: String
)

data class BeneficiariosResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<Beneficiario>
)