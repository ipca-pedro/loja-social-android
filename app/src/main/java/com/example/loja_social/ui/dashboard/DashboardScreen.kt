package com.example.loja_social.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.loja_social.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToAlerts: () -> Unit,
    onNavigateToEntregas: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
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
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.errorMessage != null) {
                ErrorCard(errorMessage = uiState.errorMessage!!)
            } else {
                DashboardContent(uiState, onNavigateToAlerts, onNavigateToEntregas)
            }
        }
    }
}

@Composable
fun DashboardContent(
    uiState: DashboardUiState, 
    onNavigateToAlerts: () -> Unit, 
    onNavigateToEntregas: () -> Unit
) {
    if (uiState.isLoading && uiState.alertas.isEmpty()) {
        LoadingState()
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Cards principais com métricas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LojaSocialCard(
                    title = "Alertas de Validade",
                    subtitle = "Produtos críticos",
                    value = uiState.alertas.size.toString(),
                    icon = Icons.Default.Warning,
                    trend = if (uiState.alertas.size > 5) -1f else 1f,
                    gradient = true,
                    onClick = onNavigateToAlerts,
                    modifier = Modifier.weight(1f)
                )
                
                LojaSocialCard(
                    title = "Entregas Hoje",
                    subtitle = "Agendadas",
                    value = uiState.entregasAgendadasCount.toString(),
                    icon = Icons.Default.List,
                    trend = 0.5f,
                    onClick = onNavigateToEntregas,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Cards adicionais
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LojaSocialCard(
                    title = "Stock Status",
                    subtitle = "Produtos OK",
                    value = "85",
                    percentage = 85f,
                    status = "Bom",
                    modifier = Modifier.weight(1f)
                )
                
                LojaSocialCard(
                    title = "Beneficiários",
                    subtitle = "Ativos este mês",
                    value = "24",
                    trend = 2f,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Gráficos
            SimpleBarChart(
                data = listOf(
                    "Alimentar" to 45,
                    "Higiene" to 23,
                    "Limpeza" to 18,
                    "Outros" to 12
                )
            )
        }
    }
}

@Composable
fun ErrorCard(errorMessage: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
