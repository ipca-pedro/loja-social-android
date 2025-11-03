package com.example.loja_social.api

import com.google.gson.annotations.SerializedName

/**
 * Esta é a nova classe "envelope".
 * É isto que a API realmente devolve.
 */
data class CampanhasResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: List<Campanha> // A nossa lista de campanhas está aqui dentro
)

/**
 * Esta é a classe 'Campanha' CORRIGIDA.
 */
data class Campanha(
    @SerializedName("id")
    val id: String, // IMPORTANTE: O seu ID é um Texto (String), não um Número (Int)

    @SerializedName("nome")
    val nome: String,

    @SerializedName("descricao")
    val descricao: String?,

    @SerializedName("data_inicio")
    val dataInicio: String,

    @SerializedName("data_fim")
    val dataFim: String

    // O campo 'ativa' não estava no JSON, por isso foi removido.
)