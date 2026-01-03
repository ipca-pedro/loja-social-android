package com.example.loja_social.ui.beneficiario

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.loja_social.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeneficiarioMainScreen(
    viewModel: BeneficiarioMainViewModel,
    onLogoutClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Minhas Entregas") },
                actions = {
                    IconButton(onClick = onLogoutClick) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sair")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Campanhas Ativas
            item {
                LojaSocialCard(
                    title = "Campanhas Ativas",
                    subtitle = "campanhas disponíveis",
                    value = "${uiState.campanhasAtivas.size}",
                    icon = Icons.Default.Campaign
                )
            }
            
            // Minhas Entregas
            item {
                Text(
                    text = "Minhas Entregas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            when {
                uiState.isLoading -> {
                    item { LoadingState() }
                }
                uiState.errorMessage != null -> {
                    item {
                        Text(
                            uiState.errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                uiState.minhasEntregas.isEmpty() -> {
                    item {
                        EmptyState(
                            title = "Nenhuma entrega encontrada",
                            subtitle = "Não tem entregas agendadas no momento",
                            icon = Icons.AutoMirrored.Filled.Assignment
                        )
                    }
                }
                else -> {
                    items(uiState.minhasEntregas) { entrega ->
                        BeneficiarioEntregaItem(entrega = entrega)
                    }
                }
            }
        }
    }
}

@Composable
fun BeneficiarioEntregaItem(
    entrega: com.example.loja_social.api.Entrega
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Entrega #${entrega.id.substring(0, 8)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = entrega.dataAgendamento,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Colaborador: ${entrega.colaborador}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusChip(status = entrega.estado)
            }
        }
    }
}