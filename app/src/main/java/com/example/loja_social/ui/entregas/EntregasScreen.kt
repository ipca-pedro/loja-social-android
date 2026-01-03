package com.example.loja_social.ui.entregas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.* 
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.loja_social.api.Entrega

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntregasScreen(
    viewModel: EntregasViewModel,
    onAgendarClick: () -> Unit,
    onNavigateBack: () -> Unit,
    onEntregaClick: (id: String, estado: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestão de Entregas") },
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
            FloatingActionButton(onClick = onAgendarClick) {
                Icon(Icons.Default.Add, contentDescription = "Agendar Nova Entrega")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = uiState.selectedTab.ordinal) {
                EntregaFilterType.values().forEach { tab ->
                    Tab(
                        selected = uiState.selectedTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        text = { Text(tab.name.replace("_", " "), maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    )
                }
            }

            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.filteredEntregas, key = { it.id }) { entrega ->
                    EntregaCard(
                        entrega = entrega, 
                        onClick = { onEntregaClick(entrega.id, entrega.estado) },
                        onConcluirClick = {
                        viewModel.concluirEntrega(entrega.id)
                    })
                }
            }
        }
    }
}

@Composable
fun EntregaCard(entrega: Entrega, onClick: () -> Unit, onConcluirClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = "Beneficiário: ${entrega.beneficiario}", style = MaterialTheme.typography.titleMedium)
            Text(text = "Nº Estudante: ${entrega.numEstudante ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Data: ${entrega.dataAgendamento.substringBefore("T")}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Estado: ${entrega.estado}", style = MaterialTheme.typography.bodyMedium)
            
            if (entrega.estado == "agendada") {
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onConcluirClick, modifier = Modifier.fillMaxWidth()) {
                    Text("CONCLUIR ENTREGA")
                }
            }
        }
    }
}
