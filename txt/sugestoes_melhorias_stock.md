# 🚀 Sugestões de Melhorias para Gestão de Stock

## ✅ Problema Corrigido
- **Texto duplicado na data**: Removido o hint duplicado do `TextInputEditText`

---

## 📋 Funcionalidades que Podem ser Adicionadas

### 1. **Lista de Stock Existente** ⭐ (Prioridade Alta)
**O que fazer:**
- Criar um novo Fragment `StockListFragment` para mostrar todos os produtos em stock
- Exibir: Nome do produto, categoria, quantidade total, número de lotes, validade mais próxima
- Usar `RecyclerView` com cards estilizados (similar aos beneficiários/entregas)
- Adicionar `SwipeRefreshLayout` para atualizar

**API já disponível:**
- `GET /api/admin/stock` - retorna lista agrupada por produto

**Benefícios:**
- Ver stock atual antes de adicionar
- Identificar produtos com stock baixo
- Ver produtos próximos do vencimento

---

### 2. **Detalhes de um Produto em Stock** ⭐⭐ (Prioridade Alta)
**O que fazer:**
- Ao clicar num item da lista, mostrar detalhes:
  - Todos os lotes desse produto
  - Quantidade de cada lote
  - Data de validade de cada lote
  - Data de entrada
  - Opções para editar/remover cada lote

**Benefícios:**
- Gestão granular do stock
- Ver histórico de entradas
- Identificar lotes específicos para entregas

---

### 3. **Editar Lotes de Stock** ⭐⭐ (Prioridade Alta)
**O que fazer:**
- Criar `EditStockFragment` ou dialog
- Permitir editar:
  - Quantidade atual
  - Data de validade
- Validar que quantidade atual ≤ quantidade inicial

**API já disponível:**
- `PUT /api/admin/stock/:id`

**Benefícios:**
- Corrigir erros de registo
- Atualizar quantidades após inventário físico
- Ajustar datas de validade

---

### 4. **Remover Lotes de Stock** ⭐ (Prioridade Média)
**O que fazer:**
- Adicionar botão "Remover" nos detalhes do lote
- Dialog de confirmação antes de remover
- Mostrar mensagem de sucesso/erro

**API já disponível:**
- `DELETE /api/admin/stock/:id`

**Benefícios:**
- Remover lotes vazios ou incorretos
- Limpar stock obsoleto

---

### 5. **Alertas de Validade** ⭐⭐⭐ (Prioridade Muito Alta)
**O que fazer:**
- Criar `AlertasValidadeFragment`
- Mostrar produtos com validade nos próximos 30 dias
- Ordenar por data mais próxima
- Cores diferentes:
  - Vermelho: já vencido
  - Laranja: vence nos próximos 7 dias
  - Amarelo: vence nos próximos 30 dias
- Badge no Dashboard com número de alertas

**API já disponível:**
- `GET /api/admin/alertas/validade`

**Benefícios:**
- Evitar desperdício
- Priorizar entregas de produtos próximos do vencimento
- Planeamento de campanhas

---

### 6. **Filtros e Pesquisa** ⭐ (Prioridade Média)
**O que fazer:**
- Barra de pesquisa por nome de produto
- Filtro por categoria
- Filtro por stock baixo (ex: < 10 unidades)
- Filtro por produtos próximos do vencimento
- Ordenação: nome, quantidade, validade

**Benefícios:**
- Encontrar produtos rapidamente
- Focar em produtos que precisam de atenção

---

### 7. **Estatísticas Rápidas** ⭐ (Prioridade Baixa)
**O que fazer:**
- Cards no topo da lista de stock:
  - Total de produtos diferentes
  - Total de unidades em stock
  - Produtos com stock baixo
  - Alertas de validade
  - Produtos sem validade

**Benefícios:**
- Visão geral rápida
- Identificar problemas rapidamente

---

### 8. **Histórico de Movimentações** ⭐ (Prioridade Baixa)
**O que fazer:**
- Mostrar quando cada lote foi adicionado
- Mostrar quando foi usado em entregas
- Timeline de movimentações

**Nota:** Pode precisar de nova tabela/endpoint no servidor

---

### 9. **Validações Melhoradas** ⭐ (Prioridade Média)
**O que fazer:**
- Validar que quantidade > 0
- Validar que data de validade não é no passado (ou avisar se for)
- Sugerir data mínima (hoje)
- Validar formato de data antes de enviar

**Benefícios:**
- Menos erros
- Melhor UX

---

### 10. **Integração com Dashboard** ⭐⭐ (Prioridade Média)
**O que fazer:**
- Mostrar no Dashboard:
  - Número de produtos em stock
  - Produtos com stock baixo
  - Alertas de validade
  - Últimos produtos adicionados

**Benefícios:**
- Visão geral centralizada
- Acesso rápido a informações importantes

---

## 🎨 Melhorias de UI/UX

### 1. **Feedback Visual Melhorado**
- Animações ao adicionar stock
- Progress indicators mais visíveis
- Mensagens de sucesso mais destacadas

### 2. **Campos Inteligentes**
- Auto-completar baseado em produtos recentes
- Sugestões de quantidade baseadas em histórico
- Lembrar última categoria/produto selecionado

### 3. **Ações Rápidas**
- Botão "Adicionar mais do mesmo" após sucesso
- Atalhos para produtos mais usados
- Templates de stock comum

---

## 📱 Estrutura Sugerida de Navegação

```
Stock (Tab)
├── Adicionar Stock (atual)
├── Lista de Stock (NOVO)
│   ├── Ver Detalhes (NOVO)
│   │   ├── Editar Lote (NOVO)
│   │   └── Remover Lote (NOVO)
│   └── Filtros/Pesquisa (NOVO)
└── Alertas de Validade (NOVO)
```

---

## 🚀 Ordem de Implementação Recomendada

1. **Fase 1 (Essencial):**
   - ✅ Adicionar Stock (já feito)
   - ⭐ Lista de Stock
   - ⭐ Alertas de Validade

2. **Fase 2 (Importante):**
   - ⭐⭐ Detalhes de Produto
   - ⭐⭐ Editar Lotes
   - ⭐ Remover Lotes

3. **Fase 3 (Melhorias):**
   - ⭐ Filtros e Pesquisa
   - ⭐ Estatísticas
   - ⭐ Integração com Dashboard

---

## 💡 Notas Técnicas

- Todas as APIs necessárias já existem
- Usar padrão MVVM (como já está implementado)
- Reutilizar componentes existentes (cards, adapters, etc.)
- Manter consistência com o design atual

