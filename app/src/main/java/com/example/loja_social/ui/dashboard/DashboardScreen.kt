package com.example.loja_social.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.loja_social.api.AlertaValidade
import com.example.loja_social.api.Entrega
import com.example.loja_social.ui.components.LoadingState
import com.example.loja_social.ui.components.EntregasCalendarCard
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToAlerts: (alerta: AlertaValidade) -> Unit,
    onNavigateToEntregaDetail: (entregaId: String, estado: String) -> Unit
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
        Column(modifier = Modifier.padding(padding)) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LoadingState()
                }
            } else if (uiState.errorMessage != null) {
                Box(modifier = Modifier.padding(16.dp)) {
                    ErrorCard(errorMessage = uiState.errorMessage!!)
                }
            } else {
                 DashboardTabs(uiState, viewModel, onNavigateToAlerts, onNavigateToEntregaDetail)
            }
        }
    }
}

@Composable
fun DashboardTabs(
    uiState: DashboardUiState,
    viewModel: DashboardViewModel,
    onNavigateToAlerts: (AlertaValidade) -> Unit,
    onNavigateToEntregaDetail: (String, String) -> Unit
) {
    val tabs = DashboardTab.values()
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = uiState.selectedTab.ordinal) {
            tabs.forEach { tab ->
                Tab(
                    selected = uiState.selectedTab == tab,
                    onClick = { viewModel.selectTab(tab) },
                    text = { Text(tab.title) }
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (uiState.selectedTab) {
                DashboardTab.ENTREGAS -> EntregasContent(uiState, { viewModel.selectDate(it) }, onNavigateToEntregaDetail)
                DashboardTab.ALERTAS -> AlertasContent(uiState, onNavigateToAlerts)
            }
        }
    }
}


@Composable
fun EntregasContent(
    uiState: DashboardUiState,
    onDateSelected: (LocalDate) -> Unit,
    onNavigateToEntregaDetail: (String, String) -> Unit
) {
     LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            EntregasCalendarCard(
                datasComEntregas = uiState.datasComEntregas,
                selectedDate = uiState.selectedDate,
                onDateSelected = onDateSelected
            )
        }

        if (uiState.entregasDoDiaSelecionado.isNotEmpty()) {
            item {
                 Text(
                    "Entregas para ${uiState.selectedDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                    style = MaterialTheme.typography.titleMedium,
                     modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(uiState.entregasDoDiaSelecionado, key = { it.id }) { entrega ->
                EntregaDashboardCard(
                    entrega = entrega,
                    onClick = { onNavigateToEntregaDetail(entrega.id, entrega.estado) }
                )
            }
        } else {
             item {
                 Text(
                    "Nenhuma entrega agendada para ${uiState.selectedDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                )
            }
        }
    }
}

@Composable
fun AlertasContent(uiState: DashboardUiState, onNavigateToAlerts: (AlertaValidade) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (uiState.alertasExpirados.isEmpty() && uiState.alertasCriticos.isEmpty() && uiState.alertasAtencao.isEmpty() && uiState.alertasBrevemente.isEmpty()) {
            item {
                Text(
                    text = "Sem alertas de validade.",
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        if (uiState.alertasExpirados.isNotEmpty()) {
            item { AlertaHeader("Expirado", MaterialTheme.colorScheme.error) }
            items(uiState.alertasExpirados, key = { "exp-${it.id}" }) { alerta ->
                AlertaCard(alerta = alerta, onClick = { onNavigateToAlerts(alerta) })
            }
        }

        if (uiState.alertasCriticos.isNotEmpty()) {
            item { AlertaHeader("Crítico (0-7 dias)", MaterialTheme.colorScheme.tertiary) }
            items(uiState.alertasCriticos, key = { "crit-${it.id}" }) { alerta ->
                AlertaCard(alerta = alerta, onClick = { onNavigateToAlerts(alerta) })
            }
        }

        if (uiState.alertasAtencao.isNotEmpty()) {
            item { AlertaHeader("Atenção (8-14 dias)", MaterialTheme.colorScheme.secondary) }
            items(uiState.alertasAtencao, key = { "att-${it.id}" }) { alerta ->
                AlertaCard(alerta = alerta, onClick = { onNavigateToAlerts(alerta) })
            }
        }

        if (uiState.alertasBrevemente.isNotEmpty()) {
            item { AlertaHeader("Brevemente (15-30 dias)", Color.Gray) }
            items(uiState.alertasBrevemente, key = { "soon-${it.id}" }) { alerta ->
                AlertaCard(alerta = alerta, onClick = { onNavigateToAlerts(alerta) })
            }
        }
    }
}

@Composable
fun AlertaHeader(title: String, color: Color) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun AlertaCard(alerta: AlertaValidade, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = when {
                alerta.diasRestantes < 0 -> MaterialTheme.colorScheme.error
                alerta.diasRestantes <= 7 -> MaterialTheme.colorScheme.tertiary
                alerta.diasRestantes <= 14 -> MaterialTheme.colorScheme.secondary
                else -> Color.Gray
            })
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(alerta.produto, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("Validade: ${alerta.dataValidade.substringBefore('T')}", style = MaterialTheme.typography.bodySmall)
            }
            Text(
                text = if (alerta.diasRestantes < 0) "Expirado" else "${alerta.diasRestantes} dias",
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.Bold,
                color = if (alerta.diasRestantes < 0) MaterialTheme.colorScheme.error else LocalContentColor.current
            )
        }
    }
}


// EntregasCalendarCard e Day movidos para ui/components/CalendarComponents.kt


@Composable
fun EntregaDashboardCard(entrega: Entrega, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = entrega.beneficiario, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(text = "Nº Estudante: ${entrega.numEstudante ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
            }
             Text(
                text = entrega.dataAgendamento.substringAfter('T').substring(0, 5),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
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