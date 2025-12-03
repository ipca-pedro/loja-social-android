package com.example.loja_social.ui.stock

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.loja_social.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockScreen(viewModel: StockViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    var nomeProduto by remember { mutableStateOf("") }
    var quantidade by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Adicionar Stock") },
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
            OutlinedTextField(
                value = nomeProduto,
                onValueChange = { nomeProduto = it },
                label = { Text("Nome do Produto") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = quantidade,
                onValueChange = { quantidade = it },
                label = { Text("Quantidade") },
                modifier = Modifier.fillMaxWidth()
            )

            if (uiState.isLoading) {
                LoadingState()
            } else {
                Text(
                    "Selecione um produto existente ou adicione um novo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (uiState.errorMessage != null) {
                Text(
                    uiState.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            if (uiState.successMessage != null) {
                Text(
                    uiState.successMessage!!,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = {
                    val produto = uiState.produtos.find { it.nome == nomeProduto }
                    if (produto != null) {
                        viewModel.addStockItem(produto.id, quantidade, "")
                    }
                },
                enabled = !uiState.isFormLoading && nomeProduto.isNotBlank() && quantidade.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isFormLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text("ADICIONAR STOCK")
                }
            }
        }
    }
}