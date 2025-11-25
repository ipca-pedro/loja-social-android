package com.example.loja_social.ui.entregas

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun EntregasScreen(viewModel: EntregasViewModel, onAgendarClick: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf<com.example.loja_social.api.Entrega?>(null) }

    // Feedback para o utilizador (Toast)
    val context = LocalContext.current
    LaunchedEffect(uiState.actionSuccessMessage) {
        uiState.actionSuccessMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearActionMessage() // Limpa a mensagem para não a mostrar novamente
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Gestão de Entregas") }) },
        floatingActionButton = {
            if (!uiState.isLoading) {
                FloatingActionButton(onClick = onAgendarClick) {
                    Icon(Icons.Default.Add, contentDescription = "Agendar Entrega")
                }
            }
        }
    ) { padding ->
        val pullRefreshState = rememberPullRefreshState(uiState.isLoading, { viewModel.fetchEntregas() })

        Box(
            modifier = Modifier
                .padding(padding)
                .pullRefresh(pullRefreshState)
                .fillMaxSize()
        ) {
            when {
                // UI para o estado de carregamento inicial (não swipe)
                uiState.isLoading && uiState.entregas.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                // UI para quando não há entregas
                uiState.entregas.isEmpty() -> {
                    Text(
                        text = "Nenhuma entrega agendada. \nToque em '+' para adicionar uma nova entrega.",
                        modifier = Modifier.align(Alignment.Center), 
                        textAlign = TextAlign.Center
                    )
                }
                // UI para a lista de entregas
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.entregas) {
                            EntregaCard(entrega = it, onConfirmClick = { showDialog = it })
                        }
                    }
                }
            }
            
            // Indicador de "Puxar para atualizar"
            PullRefreshIndicator(uiState.isLoading, pullRefreshState, Modifier.align(Alignment.TopCenter))
        }

        // Diálogo de confirmação
        if (showDialog != null) {
            ConfirmacaoDialog(showDialog!!, viewModel, onDismiss = { showDialog = null })
        }
    }
}

@Composable
fun ConfirmacaoDialog(entrega: com.example.loja_social.api.Entrega, viewModel: EntregasViewModel, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmar Entrega") },
        text = {
            Text("Tem a certeza que deseja confirmar esta entrega?" +
                 "\n\nBeneficiário: ${entrega.beneficiario}" +
                 "\nData: ${entrega.dataAgendamento}" + 
                 "\n\nO stock será automaticamente abatido.")
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.concluirEntrega(entrega.id)
                    onDismiss()
                }
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun EntregaCard(entrega: com.example.loja_social.api.Entrega, onConfirmClick: () -> Unit) {
    Card(elevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Beneficiário: ${entrega.beneficiario}", fontWeight = FontWeight.Bold)
            Text("Data: ${entrega.dataAgendamento}")
            Text("Estado: ${entrega.estado}", color = if (entrega.estado == "Pendente") Color.Red else Color.Green)
            Spacer(modifier = Modifier.height(8.dp))
            if (entrega.estado == "Pendente") {
                Button(onClick = onConfirmClick, modifier = Modifier.align(Alignment.End)) {
                    Text("Confirmar Entrega")
                }
            }
        }
    }
}

