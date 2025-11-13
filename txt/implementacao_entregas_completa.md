# 📦 Implementação Completa do Fluxo de Entregas

## ✅ **CONFIRMAÇÃO**

**Tabela:** `stock_items` ✅  
**Fluxo:** Confirmado ✅

---

## 🎯 **FLUXO DE AGENDAMENTO**

### 1. **Buscar Lotes Disponíveis**
- Endpoint: `GET /api/admin/stock/lotes`
- Retorna: Lista de `LoteIndividual` com `id`, `quantidade_atual`, `produto`, etc.
- Filtro: Apenas lotes com `quantidade_atual > 0`

### 2. **Seleção de Múltiplos Lotes**
- UI permite selecionar vários lotes
- Para cada lote, definir `quantidade_entregue` (≤ `quantidade_atual`)

### 3. **Mapeamento para Request**
```kotlin
AgendarEntregaRequest(
    beneficiarioId = "...",
    dataAgendamento = "2025-01-15",
    itens = listOf(
        AgendarEntregaItemRequest(
            stock_item_id = "uuid-lote-1",
            quantidade_entregue = 5
        ),
        AgendarEntregaItemRequest(
            stock_item_id = "uuid-lote-2",
            quantidade_entregue = 10
        )
    )
)
```

### 4. **Enviar ao Servidor**
- `POST /api/admin/entregas`
- Servidor cria `entrega` e `detalhes_entrega` (um por item)

---

## 🔄 **FLUXO DE CONFIRMAÇÃO**

### 1. **Listar Entregas Agendadas**
- `GET /api/admin/entregas`
- Mostrar apenas entregas com `estado = 'agendada'`

### 2. **Confirmar Entrega**
- Botão "Concluir" na lista
- **NOVO:** Dialog de confirmação antes de concluir
- `PUT /api/admin/entregas/:id/concluir`
- Servidor atualiza `estado = 'entregue'` e o trigger abate o stock

---

## 📋 **ESTRUTURA DE DADOS**

### Tabela `stock_items`
```sql
- id (UUID) → stock_item_id no AgendarEntregaItemRequest
- quantidade_atual (INT) → máximo para quantidade_entregue
- produto_id (INT)
- data_validade (DATE, nullable)
```

### Tabela `entregas`
```sql
- id (UUID)
- beneficiario_id (UUID)
- colaborador_id (UUID)
- data_agendamento (DATE)
- estado ('agendada' | 'entregue')
```

### Tabela `detalhes_entrega`
```sql
- entrega_id (UUID) → FK para entregas
- stock_item_id (UUID) → FK para stock_items
- quantidade_entregue (INT)
```

---

## ✅ **IMPLEMENTAÇÃO**

### Servidor (admin.txt)
- ✅ `POST /api/admin/entregas` - Já existe
- ✅ `GET /api/admin/entregas` - Já existe
- ✅ `PUT /api/admin/entregas/:id/concluir` - Já existe
- ⚠️ `GET /api/admin/stock/lotes` - **PRECISA SER CRIADO** (sugestão em `sugestao_endpoint_todos_lotes.txt`)

### Android
- ✅ `AgendarEntregaRequest` - Já existe
- ✅ `AgendarEntregaItemRequest` - Já existe
- ⚠️ `AgendarEntregaViewModel` - **PRECISA BUSCAR LOTES**
- ⚠️ `AgendarEntregaFragment` - **PRECISA UI DE SELEÇÃO**
- ⚠️ `EntregasFragment` - **PRECISA CONFIRMAÇÃO ANTES DE CONCLUIR**

---

## 🎨 **UI PROPOSTA**

### AgendarEntregaFragment:
1. Selecionar Beneficiário (já existe)
2. Selecionar Data (já existe)
3. **NOVO:** Lista de Lotes Disponíveis
   - RecyclerView com lotes
   - Checkbox para selecionar
   - EditText para quantidade (máx = quantidade_atual)
   - Botão "Adicionar à Entrega"
4. **NOVO:** Lista de Itens Selecionados
   - RecyclerView com itens adicionados
   - Botão remover
   - Total de itens
5. Botão "AGENDAR ENTREGA" (só ativo se houver itens)

### EntregasFragment:
- **MELHORIA:** Dialog de confirmação antes de concluir
- Mostrar informações da entrega (beneficiário, data, itens)

---

## 📝 **PRÓXIMOS PASSOS**

1. ✅ Adicionar endpoint `/api/admin/stock/lotes` no servidor
2. ✅ Atualizar `AgendarEntregaViewModel` para buscar lotes
3. ✅ Criar UI de seleção de lotes
4. ✅ Mapear seleção para `AgendarEntregaRequest`
5. ✅ Adicionar confirmação antes de concluir entrega

---

**Status:** ✅ Fluxo confirmado e pronto para implementação


