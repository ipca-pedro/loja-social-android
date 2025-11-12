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

// --- Modelos de Autenticação ---

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("token") val token: String?
)

// --- Modelos de Admin: Beneficiários (RF2) ---

data class Beneficiario(
    @SerializedName("id") val id: String,
    @SerializedName("nome_completo") val nomeCompleto: String,
    @SerializedName("num_estudante") val numEstudante: String?,
    @SerializedName("nif") val nif: String?,
    @SerializedName("ano_curricular") val anoCurricular: Int?,
    @SerializedName("curso") val curso: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("telefone") val telefone: String?,
    @SerializedName("notas_adicionais") val notasAdicionais: String?,
    @SerializedName("estado") val estado: String,
    @SerializedName("data_registo") val dataRegisto: String?
)

data class BeneficiariosResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<Beneficiario>,
    @SerializedName("message") val message: String?
)

data class BeneficiarioRequest(
    @SerializedName("nome_completo") val nomeCompleto: String,
    @SerializedName("num_estudante") val numEstudante: String?,
    @SerializedName("nif") val nif: String?,
    @SerializedName("ano_curricular") val anoCurricular: Int?,
    @SerializedName("curso") val curso: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("telefone") val telefone: String?,
    @SerializedName("notas_adicionais") val notasAdicionais: String?,
    @SerializedName("estado") val estado: String?
)

data class SingleBeneficiarioResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: BeneficiarioData?
)

data class BeneficiarioData(
    @SerializedName("id") val id: String,
    @SerializedName("nome_completo") val nomeCompleto: String,
    @SerializedName("estado") val estado: String
)

// [NOVO] Resposta para obter um único Beneficiário (usado no Repository para obter o objeto completo)
data class FullSingleBeneficiarioResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: Beneficiario? // Contém o objeto Beneficiario completo
)


// --- Modelos de Admin: Inventário (RF3 & RF6) ---

data class Categoria(
    @SerializedName("id") val id: Int,
    @SerializedName("nome") val nome: String
)

data class CategoriasResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<Categoria>,
    @SerializedName("message") val message: String?
)

data class Produto(
    @SerializedName("id") val id: Int,
    @SerializedName("nome") val nome: String,
    @SerializedName("descricao") val descricao: String?,
    @SerializedName("categoria") val categoria: String
)

data class ProdutosResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<Produto>,
    @SerializedName("message") val message: String?
)

data class AddStockRequest(
    @SerializedName("produto_id") val produtoId: Int,
    @SerializedName("quantidade_inicial") val quantidadeInicial: Int,
    @SerializedName("data_validade") val dataValidade: String?,
    @SerializedName("campanha_id") val campanhaId: String?
)

data class AddStockResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: StockItemData?
)

data class StockItemData(
    @SerializedName("id") val id: String,
    @SerializedName("quantidade_inicial") val quantidadeInicial: Int,
    @SerializedName("data_validade") val dataValidade: String?
)

data class AlertaValidade(
    @SerializedName("id") val id: String,
    @SerializedName("produto") val produto: String,
    @SerializedName("quantidade_atual") val quantidadeAtual: Int,
    @SerializedName("data_validade") val dataValidade: String,
    @SerializedName("dias_restantes") val diasRestantes: Int
)

data class AlertasValidadeResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<AlertaValidade>,
    @SerializedName("message") val message: String?
)

// --- Modelos de Admin: Entregas (RF4) ---

data class Entrega(
    @SerializedName("id") val id: String,
    @SerializedName("data_agendamento") val dataAgendamento: String,
    @SerializedName("estado") val estado: String,
    @SerializedName("beneficiario") val beneficiario: String,
    @SerializedName("num_estudante") val numEstudante: String?,
    @SerializedName("colaborador") val colaborador: String
)

data class EntregasResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<Entrega>,
    @SerializedName("message") val message: String?
)

data class AgendarEntregaRequest(
    @SerializedName("beneficiario_id") val beneficiarioId: String,
    @SerializedName("data_agendamento") val dataAgendamento: String,
    @SerializedName("itens") val itens: List<AgendarEntregaItemRequest>
)

data class AgendarEntregaItemRequest(
    @SerializedName("stock_item_id") val stockItemId: String,
    @SerializedName("quantidade_entregue") val quantidadeEntregue: Int
)

data class AgendarEntregaResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: EntregaAgendadaData?
)

data class EntregaAgendadaData(
    @SerializedName("id") val id: String
)

data class ConcluirEntregaResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: EntregaConcluidaData?
)

data class EntregaConcluidaData(
    @SerializedName("id") val id: String,
    @SerializedName("estado") val estado: String
)