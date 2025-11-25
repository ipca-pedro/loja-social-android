package com.example.loja_social.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val pullRefreshState = rememberPullRefreshState(uiState.isLoading, { viewModel.refresh() })

    Scaffold(
        topBar = { TopAppBar(title = { Text("Dashboard") }) }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .pullRefresh(pullRefreshState)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (uiState.errorMessage != null) {
                    ErrorCard(errorMessage = uiState.errorMessage!!)
                } else {
                    DashboardContent(uiState)
                }
            }

            PullRefreshIndicator(uiState.isLoading, pullRefreshState, Modifier.align(Alignment.TopCenter))
        }
    }
}

@Composable
fun DashboardContent(uiState: DashboardUiState) {
    if (uiState.isLoading && uiState.alertas.isEmpty()) {
        // Mostra o indicador de progresso apenas no carregamento inicial
        CircularProgressIndicator(modifier = Modifier.padding(top = 64.dp))
    } else {
        Column {
            DashboardCard("Alertas de Validade", uiState.alertas.size.toString())
            Spacer(modifier = Modifier.height(16.dp))
            DashboardCard("Entregas Agendadas", uiState.entregasAgendadasCount.toString())
        }
    }
}

@Composable
fun DashboardCard(title: String, count: String) {
    Card(
        elevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = title, style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = count, style = MaterialTheme.typography.h4, fontSize = 48.sp)
        }
    }
}

@Composable
fun ErrorCard(errorMessage: String) {
    Card(
        elevation = 4.dp,
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colors.error
    ) {
        Text(
            text = errorMessage,
            color = MaterialTheme.colors.onError,
            modifier = Modifier.padding(16.dp),
            textAlign = TextAlign.Center
        )
    }
}

