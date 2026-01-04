package com.example.loja_social.ui.entregas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.compose.material.icons.filled.Edit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntregaDetailScreen(
    viewModel: EntregaDetailViewModel,
    estado: String,
    onNavigateBack: () -> Unit,
    onEditClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes da Entrega") },
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
        },
        floatingActionButton = {
            if (estado == "agendada") {
                FloatingActionButton(onClick = onEditClick) {
                    Icon(Icons.Filled.Edit, contentDescription = "Editar Entrega")
                }
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(padding))
        }

        uiState.errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(padding).padding(16.dp))
        }

        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.entregaItems) { item ->
                EntregaDetailItemCard(item = item)
            }
        }
    }
}

@Composable
fun EntregaDetailItemCard(item: com.example.loja_social.api.EntregaDetailItem) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = item.produto, style = MaterialTheme.typography.titleMedium)
            Text(text = "Quantidade: ${item.quantidadeEntregue}", style = MaterialTheme.typography.bodyMedium)
            item.categoria?.let {
                Text(text = "Categoria: $it", style = MaterialTheme.typography.bodyMedium)
            }
            item.dataValidade?.let {
                Text(text = "Validade: ${it.substringBefore("T")}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
