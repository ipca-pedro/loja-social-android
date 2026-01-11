package com.example.loja_social.ui.beneficiario

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Notifications
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
    viewModel: BeneficiarioMainViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Área do Beneficiário") },

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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Campanhas Ativas",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "campanhas disponíveis",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "${uiState.campanhasAtivas.size}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // Tarefa 1: Calendário Personalizado
            item {
                Text(
                    text = "Minhas Entregas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                com.example.loja_social.ui.components.EntregasCalendarCard(
                    datasComEntregas = uiState.datasComEntregas,
                    selectedDate = uiState.selectedDate,
                    onDateSelected = { viewModel.selectDate(it) }
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
                uiState.entregasDoDia.isEmpty() -> {
                    item {
                        EmptyState(
                            title = "Sem entregas neste dia",
                            subtitle = "Selecione outro dia no calendário",
                            icon = Icons.AutoMirrored.Filled.Assignment
                        )
                    }
                }
                else -> {
                    item {
                        Text(
                            "Entregas para ${uiState.selectedDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    items(uiState.entregasDoDia) { entrega ->
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
                        text = "Entrega Agendada",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    val formattedDate = remember(entrega.dataAgendamento) {
                        try {
                            val inputFormatter = java.time.format.DateTimeFormatter.ISO_DATE_TIME
                            val outputFormatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
                            java.time.LocalDateTime.parse(entrega.dataAgendamento, inputFormatter).format(outputFormatter)
                        } catch (e: Exception) {
                            try {
                                val inputFormatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                val outputFormatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                java.time.LocalDateTime.parse(entrega.dataAgendamento, inputFormatter).format(outputFormatter)
                            } catch (e2: Exception) {
                                entrega.dataAgendamento.split("T").firstOrNull()?.split(" ")?.firstOrNull() ?: entrega.dataAgendamento
                            }
                        }
                    }

                    Text(
                        text = formattedDate,
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