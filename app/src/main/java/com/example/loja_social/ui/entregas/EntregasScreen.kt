package com.example.loja_social.ui.entregas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.loja_social.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntregasScreen(
    viewModel: EntregasViewModel,
    onAgendarClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Entregas") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAgendarClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agendar Entrega")
            }
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
                uiState.entregas.isEmpty() -> {
                    EmptyState(
                        title = "Nenhuma entrega encontrada",
                        subtitle = "Agende a primeira entrega usando o botão +",
                        icon = Icons.Default.Assignment
                    )
                }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(uiState.entregas) { entrega ->
                            LojaSocialListItem(
                                title = "Entrega #${entrega.id.substring(0, 8)}",
                                subtitle = "Beneficiário: ${entrega.beneficiario}",
                                trailing = entrega.dataAgendamento,
                                status = entrega.estado,
                                onClick = { /* Navigate to detail */ }
                            )
                        }
                    }
                }
            }
        }
    }
}