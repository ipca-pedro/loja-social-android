package com.example.loja_social.ui.stock

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.loja_social.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDetailScreen(
    viewModel: StockDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes do Stock") },
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
                        value = item.quantidadeTotal.toString(),
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
                            items(uiState.lotes) { lote ->
                                LojaSocialListItem(
                                    title = "Lote #${lote.id}",
                                    subtitle = "Quantidade: ${lote.quantidadeAtual}",
                                    trailing = lote.dataValidade?.substringBefore("T") ?: "Sem validade",
                                    onClick = { /* Handle lote click */ }
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