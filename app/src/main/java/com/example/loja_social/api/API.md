# 📖 Documentação da API Loja Social (para Android)

Este documento detalha todos os endpoints da API da Loja Social, como autenticar e que dados esperar.

## Informações Base
- *URL Base*: https://api-lojasocial.duckdns.org
- *Formato*: JSON

## 🔐 Autenticação

A API usa Tokens JWT (Bearer Token) para proteger as rotas de administração.

1. *Obter Token*: Primeiro, faça POST para /api/auth/login com email e password.
2. *Guardar Token*: A API devolverá um token. Guarde este token no seu SessionManager (SharedPreferences).
3. *Enviar Token*: Para todas as rotas na secção 🛡 Rotas de Admin, tem de adicionar o seguinte Header HTTP ao seu pedido: Authorization: Bearer <o_token_que_guardou>

*(A boa notícia é que o AuthInterceptor que criámos no Android Studio já faz isto automaticamente para todas as rotas que comecem por /api/admin/).*

---

## 1. 🔑 Rotas de Autenticação

Usado para obter o seu Token.

### POST /api/auth/login
Faz o login do colaborador e devolve um token de autenticação.

*Autenticação*: Nenhuma.

*Body (JSON a Enviar)*:
json
{
"email": "admin@lojasocial.pt",
"password": "password123"
}


*Resposta de Sucesso (200 OK)*:
json
{
"success": true,
"message": "Login realizado com sucesso",
"token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6Ij..."
}


*Resposta de Erro (401 Unauthorized)*:
json
{
"success": false,
"message": "Credenciais inválidas"
}


### GET /api/auth/me
Verifica o utilizador autenticado.

*Autenticação*: Bearer Token

*Resposta de Sucesso (200 OK)*:
json
{
"success": true,
"data": {
"id": "uuid-...",
"nome": "Admin Loja Social",
"email": "admin@lojasocial.pt"
}
}


---

## 2. 🌍 Rotas Públicas

Estas rotas podem ser acedidas por qualquer pessoa, a qualquer altura, sem token.

### GET /api/public/campanhas
Lista todas as campanhas de doação.

*Autenticação*: Nenhuma.

*Resposta de Sucesso (200 OK)*:
json
{
"success": true,
"data": [
{
"id": "uuid-...",
"nome": "Campanha Natal Solidário 2024",
"descricao": "Recolha de bens...",
"data_inicio": "2024-11-15T00:00:00.000Z",
"data_fim": "2024-12-20T00:00:00.000Z"
}
]
}


### GET /api/public/stock-summary
Devolve um resumo do stock "seguro" (sem quantidades exatas), que vem da View public_stock_summary da BD.

*Autenticação*: Nenhuma.

*Resposta de Sucesso (200 OK)*:
json
{
"success": true,
"data": [
{
"categoria": "Enlatados",
"produto": "Atum em Óleo",
"disponibilidade": 1,
"validade_proxima": "2026-10-31T00:00:00.000Z"
}
]
}


### POST /api/public/contacto
Envia uma mensagem do formulário de contacto.

*Autenticação*: Nenhuma.

*Body (JSON a Enviar)*:
json
{
"nome": "Utilizador Teste",
"email": "teste@gmail.com",
"mensagem": "Isto é uma mensagem de teste."
}


*Resposta de Sucesso (201 Created)*:
json
{
"success": true,
"message": "Mensagem enviada com sucesso",
"data": {
"id": 12
}
}


---

## 3. 🛡 Rotas de Admin (Protegidas)

Requerem todas o Header Authorization: Bearer <token>! (O seu index.js aplica-lhes o prefixo /api/admin/)

### Gestão de Beneficiários (RF2)

#### GET /api/admin/beneficiarios
Lista todos os beneficiários registados.

*Resposta de Sucesso (200 OK)*:
json
{
"success": true,
"data": [
{
"id": "uuid-...",
"nome_completo": "João Silva",
"num_estudante": "a12345",
"nif": "123456789",
"ano_curricular": 2,
"curso": "Engenharia Informática",
"email": "a12345@ipca.pt",
"telefone": "912345678",
"notas_adicionais": "Alergia a frutos secos",
"estado": "ativo",
"data_registo": "..."
}
]
}


#### POST /api/admin/beneficiarios
Cria um novo beneficiário.

*Body (JSON a Enviar)*:
json
{
"nome_completo": "Novo Aluno",
"num_estudante": "a99999",
"nif": "111222333",
"ano_curricular": 1,
"curso": "Design",
"email": "a99999@ipca.pt",
"telefone": "900000000",
"notas_adicionais": "Nenhuma nota"
}


*Resposta de Sucesso (201 Created)*:
json
{
"success": true,
"message": "Beneficiário criado com sucesso",
"data": {
"id": "uuid-...",
"nome_completo": "Novo Aluno",
"num_estudante": "a99999",
"estado": "ativo"
}
}


#### PUT /api/admin/beneficiarios/:id
Atualiza os dados de um beneficiário (use isto para editar ou para "reativar", mudando o estado).

*Body (JSON a Enviar)*: (Envie todos os campos)
json
{
"nome_completo": "Novo Aluno (Editado)",
"num_estudante": "a99999",
"nif": "111222333",
"ano_curricular": 2,
"curso": "Design",
"email": "a99999@ipca.pt",
"telefone": "900000000",
"estado": "inativo",
"notas_adicionais": "Editado"
}


*Resposta de Sucesso (200 OK)*:
json
{
"success": true,
"message": "Beneficiário atualizado com sucesso",
"data": {
"id": "uuid-...",
"nome_completo": "Novo Aluno (Editado)",
"estado": "inativo"
}
}


### Gestão de Inventário (RF3 & RF6)

#### GET /api/admin/categorias
Lista as categorias de produtos (útil para dropdowns na app).

*Resposta de Sucesso (200 OK)*:
json
{
"success": true,
"data": [
{ "id": 1, "nome": "Enlatados" },
{ "id": 2, "nome": "Massas e Arroz" }
]
}


#### GET /api/admin/produtos
Lista os "tipos" de produto (útil para dropdowns na app).

*Resposta de Sucesso (200 OK)*:
json
{
"success": true,
"data": [
{
"id": 1,
"nome": "Atum em Óleo",
"descricao": "Lata de atum...",
"categoria": "Enlatados"
}
]
}


#### POST /api/admin/stock
Adiciona um novo "lote" de stock ao inventário. O colaborador_id é retirado do token automaticamente.

*Body (JSON a Enviar)*:
json
{
"produto_id": 1,
"quantidade_inicial": 100,
"data_validade": "2026-12-31",
"campanha_id": "uuid-..."
}


*Resposta de Sucesso (201 Created)*:
json
{
"success": true,
"message": "Stock adicionado com sucesso",
"data": {
"id": "uuid-...",
"quantidade_inicial": 100,
"data_validade": "2026-12-31T00:00:00.000Z"
}
}


#### GET /api/admin/alertas/validade
Lista produtos que vão expirar nos próximos 30 dias (RF6).

*Resposta de Sucesso (200 OK)*:
json
{
"success": true,
"data": [
{
"id": "uuid-...",
"produto": "Salsichas Lata",
"quantidade_atual": 40,
"data_validade": "...",
"dias_restantes": 25
}
]
}


### Gestão de Entregas (RF4)

#### GET /api/admin/entregas
Lista todas as entregas (agendadas, concluídas, etc.).

*Resposta de Sucesso (200 OK)*:
json
{
"success": true,
"data": [
{
"id": "uuid-...",
"data_agendamento": "2025-11-06T00:00:00.000Z",
"estado": "agendada",
"beneficiario": "Maria Pereira",
"num_estudante": "a54321",
"colaborador": "Admin Loja Social"
}
]
}


#### POST /api/admin/entregas
Agenda uma nova entrega (operação complexa com transação).

*Body (JSON a Enviar)*:
json
{
"beneficiario_id": "uuid-do-beneficiario-...",
"data_agendamento": "2025-12-01",
"itens": [
{ "stock_item_id": "uuid-do-lote-de-atum-...", "quantidade_entregue": 5 },
{ "stock_item_id": "uuid-do-lote-de-arroz-...", "quantidade_entregue": 2 }
]
}


*Resposta de Sucesso (201 Created)*:
json
{
"success": true,
"message": "Entrega agendada com sucesso",
"data": {
"id": "uuid-da-nova-entrega-..."
}
}


#### PUT /api/admin/entregas/:id/concluir
Marca uma entrega como "concluída". O Trigger na BD abate ao stock automaticamente.

*Parâmetro de URL*: :id (o ID da entrega a concluir).

*Body (JSON a Enviar)*: (Nenhum).

*Resposta de Sucesso (200 OK)*:
json
{
"success": true,
"message": "Entrega concluída com sucesso",
"data": {
"id": "uuid-da-entrega-...",
"estado": "entregue"
}
}


---

## 4. 🔧 Utilitários

### GET /health
Health check da API.

*Autenticação*: Nenhuma.

*Resposta de Sucesso (200 OK)*:
json
{
"status": "OK",
"timestamp": "2024-11-03T15:36:11.529Z",
"message": "API Loja Social funcionando"
}


---

## 📝 Notas Importantes

1. *Formato de Datas*: Todas as datas são devolvidas em formato ISO 8601 (UTC).
2. *IDs*: Todos os IDs são UUIDs, exceto alguns campos legacy que usam integers.
3. *Paginação*: Atualmente não implementada, mas pode ser adicionada no futuro.
4. *Rate Limiting*: Não implementado, mas recomendado para produção.
5. *CORS*: Configurado para aceitar pedidos do frontend React.