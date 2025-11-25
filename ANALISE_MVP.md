# Análise do MVP - Loja Social IPCA

## 📋 Resumo Executivo

Este documento analisa o estado atual do projeto Android da Loja Social e identifica o que está **implementado** e o que **falta** para completar o MVP (Minimum Viable Product) conforme o enunciado do projeto.

---

## ✅ O QUE JÁ ESTÁ IMPLEMENTADO

### 1. **Autenticação (RF1 - Base)**
- ✅ Login com email e password
- ✅ Sistema de tokens JWT
- ✅ SessionManager para gestão de sessão
- ✅ Interceptor de autenticação automático
- ✅ Logout com confirmação

### 2. **Gestão de Beneficiários (RF2)**
- ✅ Listagem de beneficiários com pesquisa e filtros
- ✅ Criação de novos beneficiários
- ✅ Edição de beneficiários existentes
- ✅ Visualização de detalhes do beneficiário
- ✅ Filtros por estado (ativo/inativo)
- ✅ Campos implementados: nome, número estudante, ano curricular, curso, email, telefone

### 3. **Gestão de Inventário/Stock (RF3)**
- ✅ Listagem de stock agrupado por produto
- ✅ Adicionar novo stock (criar lotes)
- ✅ Visualização de detalhes de produtos
- ✅ Visualização de lotes individuais por produto
- ✅ Edição de lotes (quantidade, data validade)
- ✅ Remoção de lotes
- ✅ Filtros por categoria
- ✅ Filtros por validade próxima e stock baixo
- ✅ Registro de: tipo produto, quantidades, data entrada, data validade
- ✅ Agrupamento de bens por categorias

### 4. **Calendarização de Apoios (RF4 - Parcial)**
- ✅ Agendamento de entregas
- ✅ Visualização de calendário/listagem de entregas
- ✅ Seleção de beneficiário e data
- ✅ Seleção de bens para entrega
- ⚠️ **FALTA: Visualização em formato de calendário visual**
- ❌ **FALTA: Notificações de lembretes automáticas**

### 5. **Seleção de Bens para Entrega (RF5)**
- ✅ Visualização da lista de stock disponível
- ✅ Seleção de itens para entrega
- ✅ Ajuste de quantidades por item
- ✅ Remoção de itens da lista de entrega

### 6. **Estado da Entrega (RF5)**
- ✅ Marcação de entrega como "Entregue" / "Não Entregue"
- ✅ Visualização do estado de cada entrega
- ✅ Listagem de entregas agendadas e concluídas

### 7. **Atualização Automática de Stock (RF6)**
- ✅ Atualização automática após conclusão de entrega
- ✅ Abatimento de stock quando entrega é marcada como concluída

### 8. **Alertas de Validade (RF7 - Parcial)**
- ✅ Sistema de alertas visuais (chips coloridos) nos produtos
- ✅ Exibição de alertas no Dashboard
- ✅ Cálculo de dias até vencimento
- ✅ API endpoint para obter alertas (`/api/admin/alertas/validade`)
- ⚠️ **FALTA: Geração de relatórios para priorizar distribuição**

---

## ❌ O QUE FALTA PARA O MVP

### 🔴 **CRÍTICO (Obrigatório para MVP)**

#### 1. **Notificações de Lembretes (RF4)**
**Status:** ❌ Não implementado

**O que falta:**
- Sistema de notificações push ou locais para lembretes de entregas agendadas
- Notificações antes da data de entrega (ex: 1 dia antes, no dia)
- Integração com Android NotificationManager ou WorkManager

**Sugestão de implementação:**
- Usar `WorkManager` para agendar notificações
- Criar `NotificationWorker` que verifica entregas agendadas
- Configurar notificações periódicas (diárias) para verificar entregas do dia seguinte

#### 2. **Visualização de Calendário (RF4)**
**Status:** ⚠️ Parcial - apenas listagem

**O que falta:**
- Visualização em formato de calendário visual (CalendarView ou biblioteca como `MaterialCalendarView`)
- Visualização mensal/semanal com marcação dos dias com entregas
- Navegação entre meses
- Clique em data para ver entregas do dia

**Sugestão de implementação:**
- Adicionar `MaterialCalendarView` ou `CalendarView` customizado
- Criar `CalendarFragment` ou adicionar view de calendário no `EntregasFragment`
- Marcar dias com entregas agendadas

#### 3. **Relatórios de Alertas de Validade (RF7)**
**Status:** ❌ Não implementado

**O que falta:**
- Tela/ecrã de relatórios de produtos próximos do vencimento
- Possibilidade de gerar/exportar relatório (PDF, CSV, ou visual)
- Filtros para priorizar distribuição (por data de validade, quantidade, etc.)

**Sugestão de implementação:**
- Criar `RelatoriosFragment` ou expandir `DashboardFragment`
- Adicionar botão "Ver Relatório Completo" nos alertas
- Implementar exportação (compartilhar como PDF ou CSV)

---

### 🟡 **IMPORTANTE (Melhora significativamente a experiência)**

#### 4. **Melhorias na Visualização de Entregas**
- Filtros por data, estado, beneficiário
- Ordenação (por data, beneficiário, estado)
- Busca/pesquisa de entregas

#### 5. **Validações e Feedback**
- Validação de campos obrigatórios em formulários
- Mensagens de erro mais descritivas
- Confirmações antes de ações destrutivas (já parcialmente implementado)

#### 6. **Offline/Resiliência**
- Cache local para funcionar offline
- Sincronização quando voltar online
- Tratamento robusto de erros de rede

---

### 🟢 **OPCIONAL (Nice to have, não crítico para MVP)**

#### 7. **Funcionalidades Adicionais**
- Histórico de entregas por beneficiário
- Estatísticas e gráficos no dashboard
- Exportação de dados
- Modo escuro completo (já tem suporte parcial)

---

## 📱 **WEBSITE (Fora do escopo do app Android)**

O enunciado menciona um **website informativo** para a comunidade académica. Este é um projeto separado e não faz parte do app Android. As funcionalidades do website são:
1. Gráfico com stock em tempo real
2. Campo para doação/contribuição pessoal
3. Notícias/informações sobre campanhas

**Nota:** O app Android já tem integração com a API que suporta rotas públicas (`/api/public/*`), mas o website em si precisa ser desenvolvido separadamente.

---

## 🎯 **PRIORIZAÇÃO PARA MVP**

### **Fase 1 - Crítico (Obrigatório)**
1. ✅ **Notificações de lembretes** - Sistema de notificações para entregas agendadas
2. ✅ **Visualização de calendário** - Calendário visual para ver entregas
3. ✅ **Relatórios de alertas** - Tela de relatórios de produtos próximos do vencimento

### **Fase 2 - Importante (Recomendado)**
4. Filtros e busca avançada em entregas
5. Melhorias de validação e feedback
6. Tratamento de erros mais robusto

### **Fase 3 - Opcional (Pós-MVP)**
7. Funcionalidades adicionais e melhorias de UX

---

## 📊 **RESUMO POR REQUISITO FUNCIONAL**

| RF | Funcionalidade | Status | Completude |
|---|---|---|---|
| RF1 | Autenticação | ✅ | 100% |
| RF2 | Gestão de Beneficiários | ✅ | 100% |
| RF3 | Gestão de Inventário | ✅ | 100% |
| RF4 | Calendarização de Apoios | ⚠️ | 70% - Falta calendário visual e notificações |
| RF5 | Seleção de Bens | ✅ | 100% |
| RF5 | Estado da Entrega | ✅ | 100% |
| RF6 | Atualização de Stock | ✅ | 100% |
| RF7 | Alertas de Validade | ⚠️ | 80% - Falta relatórios |

**Completude Geral do MVP: ~85%**

---

## 🔧 **PRÓXIMOS PASSOS RECOMENDADOS**

1. **Implementar notificações de lembretes** (WorkManager + NotificationManager)
2. **Adicionar visualização de calendário** (MaterialCalendarView)
3. **Criar tela de relatórios de alertas** (Fragment + exportação)
4. **Testes finais** e correção de bugs
5. **Documentação** do código e manual do utilizador

---

## 📝 **NOTAS FINAIS**

O projeto está **muito bem estruturado** e a maioria das funcionalidades core estão implementadas. As funcionalidades que faltam são principalmente relacionadas a:
- **Notificações** (sistema de lembretes)
- **Visualização** (calendário visual)
- **Relatórios** (exportação e visualização de dados)

Estas são funcionalidades importantes mas não impedem o uso básico da aplicação. Com as implementações acima, o MVP estará completo.




