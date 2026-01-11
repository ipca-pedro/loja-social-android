package com.example.loja_social.ui.stock

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.loja_social.api.LoteIndividual
import com.example.loja_social.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDetailScreen(
    viewModel: StockDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<LoteIndividual?>(null) }
    var showReportDamagedDialog by remember { mutableStateOf<LoteIndividual?>(null) }

    // Efeito para navegar para trás quando o stockDataChanged é verdadeiro
    LaunchedEffect(uiState.stockDataChanged) {
        if (uiState.stockDataChanged) {
            onNavigateBack()
        }
    }

    if (showDeleteDialog != null) {
        val loteParaApagar = showDeleteDialog!!

        DeleteLoteDialog(
            lote = loteParaApagar,
            onConfirm = {
                viewModel.deleteLote(loteParaApagar.id)
                showDeleteDialog = null
            },
            onDismiss = { showDeleteDialog = null }
        )
    }

    if (showReportDamagedDialog != null) {
        val loteParaModificar = showReportDamagedDialog!!

        ReportDamagedUnitDialog(
            lote = loteParaModificar,
            onConfirm = {
                viewModel.reportDamagedUnit(loteParaModificar)
                showReportDamagedDialog = null
            },
            onDismiss = { showReportDamagedDialog = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.stockItem?.produto ?: "Detalhes do Stock") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when {
                uiState.isLoading -> LoadingState()
                uiState.errorMessage != null -> {
                    Text(
                        uiState.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                uiState.stockItem != null -> {
                    val item = uiState.stockItem!!
                    
                    LojaSocialCard(
                        title = item.produto,
                        subtitle = "Quantidade Total",
                        value = uiState.lotes.sumOf { it.quantidadeAtual }.toString(), // Calcula a soma em tempo real
                        icon = Icons.Default.Inventory
                    )

                    Text(
                        "Lotes Disponíveis",
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    HorizontalDivider()

                    if (uiState.lotes.isEmpty()) {
                        EmptyState(
                            title = "Nenhum lote encontrado",
                            subtitle = "Este produto não possui lotes registrados",
                            icon = Icons.Default.Inventory
                        )
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    itemsIndexed(uiState.lotes) { index, lote ->
                                        // Regra de Segurança: Só permitir eliminar se estiver expirado
                                        // Se dataValidade for null, assumimos que não expira, logo não se deve apagar por "validade" (ou permitir sempre? User diz "se data atual > prazo").
                                        // Vamos assumir: Se tem data, aplica regra. Se não tem, safe to delete? Ou unsafe? 
                                        // "O botão só deve ficar clicável se a data atual for superior à data registada" implies if date exists.
                                        val isExpired = try {
                                            if (lote.dataValidade != null) {
                                                java.time.LocalDate.now().isAfter(java.time.LocalDate.parse(lote.dataValidade.substringBefore("T")))
                                            } else {
                                                false // Se não tem validade, não ativa por esta regra (ou true se quisermos permitir limpar velhos)
                                            }
                                        } catch (e: Exception) { false }

                                        LojaSocialListItem(
                                            title = "Lote ${index + 1}", // Usar numeração sequencial
                                            subtitle = "Qtd: ${lote.quantidadeAtual} | Res: ${lote.quantidadeReservada} | Dan: ${lote.quantidadeDanificada}",
                                            trailing = lote.dataValidade?.substringBefore("T") ?: "Sem validade",
                                            onClick = { /* Pode ser usado para editar no futuro */ },
                                            actions = {
                                                IconButton(
                                                    onClick = { showReportDamagedDialog = lote },
                                                    enabled = !uiState.isOperationInProgress
                                                ) {
                                                    Icon(
                                                        Icons.Default.RemoveCircleOutline, 
                                                        contentDescription = "Reportar Unidade Danificada", 
                                                        tint = if (!uiState.isOperationInProgress) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { showDeleteDialog = lote },
                                                    enabled = isExpired && !uiState.isOperationInProgress
                                                ) {
                                                    Icon(
                                                        Icons.Default.Delete, 
                                                        contentDescription = "Eliminar Lote", 
                                                        tint = if (isExpired && !uiState.isOperationInProgress) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                                    )
                                                }
                                            }
                                        )
                            }
                        }
                    }
                }
                else -> {
                    EmptyState(
                        title = "Item não encontrado",
                        subtitle = "O item de stock solicitado não foi encontrado",
                        icon = Icons.Default.Inventory
                    )
                }
            }
        }
    }
}

@Composable
fun DeleteLoteDialog(lote: LoteIndividual, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmar Remoção") },
        text = { Text("Tem a certeza que quer remover o lote #${lote.id} com ${lote.quantidadeAtual} unidades?") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { 
                Text("Remover") 
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { 
                Text("Cancelar") 
            }
        }
    )
}

@Composable
fun ReportDamagedUnitDialog(lote: LoteIndividual, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reportar Unidade Danificada") },
        text = { Text("Confirma que pretende reportar uma unidade danificada do lote #${lote.id}? A quantidade atual (${lote.quantidadeAtual}) será reduzida e a contagem de danificadas será aumentada.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
            ) { 
                Text("Confirmar") 
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { 
                Text("Cancelar") 
            }
        }
    )
}