package com.example.loja_social.api

import com.google.gson.annotations.SerializedName

/**
 * Este ficheiro contém todos os modelos de dados (data classes) usados para comunicação com a API.
 * Os modelos estão organizados por funcionalidade: públicos, autenticação, admin (beneficiários, stock, entregas).
 * Todos os campos usam @SerializedName para mapear os nomes JSON da API para propriedades Kotlin.
 */

// ===== MODELOS PÚBLICOS =====

/**
 * Resposta da API para a lista de campanhas.
 */
data class CampanhasResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<Campanha>,
    @SerializedName("message") val message: String?
)

/**
 * Modelo de uma campanha.
 */
data class Campanha(
    @SerializedName("id") val id: String,
    @SerializedName("nome") val nome: String,
    @SerializedName("descricao") val descricao: String?,
    @SerializedName("data_inicio") val dataInicio: String,
    @SerializedName("data_fim") val dataFim: String,
    @SerializedName("ativo") val ativo: Boolean? = true
)

/**
 * Requisição para criar ou atualizar uma campanha.
 */
data class CampanhaRequest(
    @SerializedName("nome") val nome: String,
    @SerializedName("descricao") val descricao: String?,
    @SerializedName("data_inicio") val dataInicio: String,
    @SerializedName("data_fim") val dataFim: String,
    @SerializedName("ativo") val ativo: Boolean?
)

/**
 * Resposta da API para uma única campanha (após criar/editar).
 */
data class SingleCampanhaResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: Campanha?
)

// ===== MODELOS DE AUTENTICAÇÃO =====

/**
 * Requisição de login (credenciais do utilizador).
 */
data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("userType") val userType: String // "admin" ou "beneficiario"
)

/**
 * Resposta da API após tentativa de login.
 * Contém o token JWT se o login for bem-sucedido.
 */
data class LoginResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("token") val token: String?,
    @SerializedName("user") val user: LoginUser?
)

/**
 * Dados do utilizador retornados no login.
 */
data class LoginUser(
    @SerializedName("id") val id: String,
    @SerializedName("nome") val nome: String,
    @SerializedName("email") val email: String,
    @SerializedName("role") val role: String,
    @SerializedName("num_estudante") val numEstudante: String?
)


// ===== MODELOS DE ADMIN: BENEFICIÁRIOS =====

/**
 * Modelo de um beneficiário (utilizador que recebe produtos).
 */
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

/**
 * Resposta da API para a lista de beneficiários.
 */
data class BeneficiariosResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<Beneficiario>,
    @SerializedName("message") val message: String?
)

/**
 * Requisição para criar ou atualizar um beneficiário.
 */
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

/**
 * Resposta da API para um único beneficiário (versão simplificada).
 */
data class SingleBeneficiarioResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: BeneficiarioData?
)

/**
 * Dados simplificados de um beneficiário (apenas ID, nome e estado).
 */
data class BeneficiarioData(
    @SerializedName("id") val id: String,
    @SerializedName("nome_completo") val nomeCompleto: String,
    @SerializedName("estado") val estado: String
)

/**
 * Resposta da API para obter um único beneficiário completo.
 * Usado no Repository para obter todos os dados do beneficiário.
 */
data class FullSingleBeneficiarioResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: Beneficiario? // Contém o objeto Beneficiario completo
)

// ===== MODELOS DE ADMIN: INVENTÁRIO (STOCK) =====

/**
 * Modelo de uma categoria de produtos.
 */
data class Categoria(
    @SerializedName("id") val id: Int,
    @SerializedName("nome") val nome: String
)

/**
 * Resposta da API para a lista de categorias.
 */
data class CategoriasResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<Categoria>,
    @SerializedName("message") val message: String?
)

/**
 * Modelo de um produto.
 */
data class Produto(
    @SerializedName("id") val id: Int,
    @SerializedName("nome") val nome: String,
    @SerializedName("descricao") val descricao: String?,
    @SerializedName("categoria") val categoria: String
)

/**
 * Resposta da API para a lista de produtos.
 */
data class ProdutosResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<Produto>,
    @SerializedName("message") val message: String?
)

/**
 * Requisição para criar um novo tipo de produto.
 */
data class CreateProductRequest(
    @SerializedName("nome") val nome: String,
    @SerializedName("descricao") val descricao: String?,
    @SerializedName("categoria_id") val categoriaId: Int
)

/**
 * Resposta da API após criar um produto.
 */
data class CreateProductResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: Produto?
)

/**
 * Requisição para adicionar novo stock (criar um lote).
 */
data class AddStockRequest(
    @SerializedName("produto_id") val produtoId: Int,
    @SerializedName("quantidade_inicial") val quantidadeInicial: Int,
    @SerializedName("data_validade") val dataValidade: String?,
    @SerializedName("campanha_id") val campanhaId: String?
)

/**
 * Resposta da API após adicionar stock.
 */
data class AddStockResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: StockItemData?
)

/**
 * Dados de um item de stock criado (lote).
 */
data class StockItemData(
    @SerializedName("id") val id: String,
    @SerializedName("quantidade_inicial") val quantidadeInicial: Int,
    @SerializedName("data_validade") val dataValidade: String?
)

/**
 * Modelo de um alerta de validade (produto próximo do vencimento).
 */
data class AlertaValidade(
    @SerializedName("id") val id: String,
    @SerializedName("produto") val produto: String,
    @SerializedName("quantidade_atual") val quantidadeAtual: Int,
    @SerializedName("data_validade") val dataValidade: String,
    @SerializedName("dias_restantes") val diasRestantes: Int
)

/**
 * Resposta da API para a lista de alertas de validade.
 */
data class AlertasValidadeResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<AlertaValidade>,
    @SerializedName("message") val message: String?
)

// ===== MODELOS DE ADMIN: ENTREGAS =====

/**
 * Modelo de uma entrega (agendada ou concluída).
 */
data class Entrega(
    @SerializedName("id") val id: String,
    @SerializedName("data_agendamento") val dataAgendamento: String,
    @SerializedName("estado") val estado: String,
    @SerializedName("beneficiario") val beneficiario: String,
    @SerializedName("num_estudante") val numEstudante: String?,
    @SerializedName("colaborador") val colaborador: String
)

/**
 * Dados do cabeçalho de uma entrega (usado na edição).
 */
data class EntregaHeader(
    @SerializedName("id") val id: String,
    @SerializedName("beneficiario_id") val beneficiarioId: String,
    @SerializedName("colaborador_id") val colaboradorId: String,
    @SerializedName("data_agendamento") val dataAgendamento: String,
    @SerializedName("estado") val estado: String
)

/**
 * Resposta da API para a lista de entregas.
 */
data class EntregasResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<Entrega>,
    @SerializedName("message") val message: String?
)

/**
 * Detalhes de um item específico de uma entrega.
 */
data class EntregaDetailItem(
    @SerializedName("id") val id: String, // stock_item_id
    @SerializedName("quantidade_entregue") val quantidadeEntregue: Int,
    @SerializedName("produto") val produto: String,
    @SerializedName("categoria") val categoria: String?,
    @SerializedName("data_validade") val dataValidade: String?,
    @SerializedName("quantidade_inicial") val quantidadeInicial: Int,
    @SerializedName("quantidade_atual") val quantidadeAtual: Int,
    @SerializedName("quantidade_reservada") val quantidadeReservada: Int,
    @SerializedName("quantidade_danificada") val quantidadeDanificada: Int,
    @SerializedName("data_entrada") val dataEntrada: String
)

/**
 * Requisição para agendar uma nova entrega.
 */
data class AgendarEntregaRequest(
    @SerializedName("beneficiario_id") val beneficiarioId: String,
    @SerializedName("colaborador_id") val colaboradorId: String,
    @SerializedName("data_agendamento") val dataAgendamento: String,
    @SerializedName("itens") val itens: List<AgendarEntregaItemRequest>
)

/**
 * Item individual de uma entrega (produto e quantidade a entregar).
 */
data class AgendarEntregaItemRequest(
    @SerializedName("stock_item_id") val stockItemId: String,
    @SerializedName("quantidade_entregue") val quantidadeEntregue: Int
)

/**
 * Resposta da API após agendar uma entrega.
 */
data class AgendarEntregaResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: EntregaAgendadaData?
)

/**
 * Dados de uma entrega agendada (apenas ID).
 */
data class EntregaAgendadaData(
    @SerializedName("id") val id: String
)

/**
 * Dados de uma entrega concluída (ID e estado final).
 */
data class ConcluirEntregaData(
    @SerializedName("id") val id: String,
    @SerializedName("estado") val estado: String
)

/**
 * Resposta da API após concluir uma entrega.
 */
data class ConcluirEntregaResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: ConcluirEntregaData?
)

// ===== WRAPPER GENÉRICO DE RESPOSTA DA API =====

/**
 * Wrapper genérico para todas as respostas da API.
 * Todas as respostas seguem o formato: {success, message, data}
 */
data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: T?
)

// ===== MODELOS DE STOCK (LISTAGEM) =====

/**
 * Resposta da API para a lista de stock agregado por produto.
 */
data class StockResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<StockItem>,
    @SerializedName("message") val message: String?
)

/**
 * Item de stock agregado (dados consolidados de um produto).
 */
data class StockItem(
    @SerializedName("produto_id") val produtoId: Int,
    @SerializedName("produto") val produto: String,
    @SerializedName("categoria") val categoria: String?,
    @SerializedName("quantidade_total") val quantidadeTotal: Int,
    @SerializedName("lotes") val lotes: Int,
    @SerializedName("validade_proxima") val validadeProxima: String?
)

// ===== MODELOS PARA ATUALIZAÇÃO/REMOÇÃO DE STOCK =====

/**
 * Requisição para atualizar um lote de stock.
 */
data class UpdateStockRequest(
    @SerializedName("quantidade_atual") val quantidadeAtual: Int?,
    @SerializedName("data_validade") val dataValidade: String?,
    @SerializedName("quantidade_danificada") val quantidadeDanificada: Int?
)

/**
 * Resposta da API após atualizar um lote de stock.
 */
data class UpdateStockResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: StockItemData?
)

/**
 * Dados retornados após reportar dano (incremento atómico).
 */
data class ReportDamageData(
    @SerializedName("id") val id: String,
    @SerializedName("quantidade_atual") val quantidadeAtual: Int,
    @SerializedName("quantidade_danificada") val quantidadeDanificada: Int,
    @SerializedName("data_validade") val dataValidade: String?
)

data class ReportDamageResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: ReportDamageData?
)

/**
 * Resposta da API após remover um lote de stock.
 */
data class DeleteStockResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?
)

// ===== MODELOS PARA LOTES INDIVIDUAIS =====

/**
 * Resposta da API para a lista de lotes individuais de um produto.
 */
data class LotesResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<LoteIndividual>,
    @SerializedName("message") val message: String?
)

/**
 * Modelo de um lote individual de stock.
 */
data class LoteIndividual(
    @SerializedName("id") val id: String,
    @SerializedName("quantidade_inicial") val quantidadeInicial: Int,
    @SerializedName("quantidade_atual") val quantidadeAtual: Int,
    @SerializedName("quantidade_reservada") val quantidadeReservada: Int = 0, // Novo campo
    @SerializedName("quantidade_danificada") val quantidadeDanificada: Int,
    @SerializedName("data_entrada") val dataEntrada: String,
    @SerializedName("data_validade") val dataValidade: String?,
    @SerializedName("produto") val produto: String,
    @SerializedName("categoria") val categoria: String?
)

// ===== MODELOS PÚBLICOS (STOCK SUMMARY) =====

/**
 * Resposta da API para o resumo público de stock.
 */
data class StockSummaryResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<StockSummaryItem>,
    @SerializedName("message") val message: String?
)

/**
 * Item do resumo público de stock.
 */
data class StockSummaryItem(
    @SerializedName("categoria") val categoria: String?,
    @SerializedName("produto") val produto: String?,
    @SerializedName("disponibilidade") val disponibilidade: Int?,
    @SerializedName("validade_proxima") val validadeProxima: String?
)

// ===== MODELOS DE BENEFICIÁRIO =====

/**
 * Resposta da API para as entregas de um beneficiário específico.
 */
data class MinhasEntregasResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<Entrega>,
    @SerializedName("message") val message: String?
)

// ===== MODELOS DE CONTACTO PÚBLICO =====

/**
 * Requisição para enviar uma mensagem de contacto.
 */
data class ContactoRequest(
    @SerializedName("nome") val nome: String?,
    @SerializedName("email") val email: String,
    @SerializedName("mensagem") val mensagem: String
)

/**
 * Resposta da API após enviar uma mensagem de contacto.
 */
data class ContactoResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: ContactoData?
)

/**
 * Dados de uma mensagem de contacto enviada (apenas ID).
 */
data class ContactoData(
    @SerializedName("id") val id: Int
)

// ===== MODELOS DE RELATÓRIOS =====

/**
 * Resposta para o relatório de entregas.
 */
data class RelatorioEntregasResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<RelatorioEntregaItem>,
    @SerializedName("message") val message: String?
)

data class RelatorioEntregaItem(
    @SerializedName("id") val id: String,
    @SerializedName("data_agendamento") val dataAgendamento: String,
    @SerializedName("estado") val estado: String,
    @SerializedName("beneficiario") val beneficiario: String?,
    @SerializedName("num_estudante") val numEstudante: String?,
    @SerializedName("colaborador") val colaborador: String?
)

/**
 * Resposta para o relatório de stock.
 */
data class RelatorioStockResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<RelatorioStockItem>,
    @SerializedName("message") val message: String?
)

data class RelatorioStockItem(
    @SerializedName("campanha_nome") val campanhaNome: String,
    @SerializedName("produto_nome") val produtoNome: String,
    @SerializedName("categoria_nome") val categoriaNome: String?,
    @SerializedName("quantidade_total") val quantidadeTotal: Int,
    @SerializedName("quantidade_recolhida") val quantidadeRecolhida: Int
)

/**
 * Resposta para o relatório de validade/lixo.
 */
data class RelatorioValidadeResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<RelatorioValidadeItem>,
    @SerializedName("message") val message: String?
)

data class RelatorioValidadeItem(
    @SerializedName("id") val id: String,
    @SerializedName("produto_nome") val produtoNome: String,
    @SerializedName("categoria_nome") val categoriaNome: String?,
    @SerializedName("quantidade_atual") val quantidadeAtual: Int,
    @SerializedName("quantidade_danificada") val quantidadeDanificada: Int,
    @SerializedName("data_validade") val dataValidade: String?,
    @SerializedName("estado_item") val estadoItem: String
)
