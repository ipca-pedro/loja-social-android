package com.example.loja_social.ui.notificacoes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.loja_social.api.RetrofitInstance
import com.example.loja_social.repository.NotificationRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacoesScreen(
    onNavigateBack: () -> Unit
) {
    val repository = remember { NotificationRepository(RetrofitInstance.api) }
    val viewModel: NotificacoesViewModel = viewModel(
        factory = NotificacoesViewModelFactory(repository)
    )
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificações") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.notificacoes.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Notifications, 
                        contentDescription = null, 
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Sem notificações", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.notificacoes) { notificacao ->
                        NotificacaoItem(
                            notificacao = notificacao,
                            onClick = { 
                                if (!notificacao.lida) {
                                    viewModel.marcarComoLida(notificacao.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificacaoItem(
    notificacao: com.example.loja_social.api.Notificacao,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (notificacao.lida) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = notificacao.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (notificacao.lida) FontWeight.Normal else FontWeight.Bold
                )
                if (!notificacao.lida) {
                    Badge(containerColor = MaterialTheme.colorScheme.primary) { Text("Nova") }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = notificacao.mensagem,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatData(notificacao.dataCriacao),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

fun formatData(isoString: String): String {
    return try {
        // Tenta fazer parse de ISO 8601
        isoString.replace("T", " ").substringBefore(".")
    } catch (e: Exception) {
        isoString
    }
}
