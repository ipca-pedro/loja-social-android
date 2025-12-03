package com.example.loja_social.ui.entregas

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.loja_social.api.Beneficiario
import com.example.loja_social.api.LoteIndividual
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendarEntregaScreen(
    viewModel: AgendarEntregaViewModel,
    onScheduleClick: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var dataAgendamento by remember { mutableStateOf("") }
    var showItemDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.onFragmentReady()
        viewModel.events.collect { event ->
            when (event) {
                is AgendarEntregaEvent.ShowSuccessMessage -> {
                    android.widget.Toast.makeText(context, event.message, android.widget.Toast.LENGTH_LONG).show()
                    onNavigateBack()
                }
            }
        }
    }

    if (showItemDialog) {
        SelecionarItemDialog(
            lotes = uiState.lotesDisponiveis,
            onDismiss = { showItemDialog = false },
            onAddItem = { lote, quantidade ->
                viewModel.adicionarItem(lote, quantidade)
                showItemDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agendar Nova Entrega") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar") } },
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
                .fillMaxSize()
        ) {
            BeneficiarioSelector(
                beneficiarios = uiState.beneficiarios,
                onBeneficiarioSelected = { beneficiario ->
                    val selectionString = "${beneficiario.nomeCompleto} (${beneficiario.numEstudante ?: "N/A"})"
                    viewModel.onBeneficiarioSelected(selectionString)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            DatePickerField(
                date = dataAgendamento,
                onDateSelected = { dataAgendamento = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Itens para Entrega", style = MaterialTheme.typography.titleLarge)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(uiState.itensSelecionados, key = { it.lote.id }) { item ->
                    ItemEntregaRow(
                        item = item,
                        onRemoveClick = { viewModel.removerItem(item.lote.id) }
                    )
                }
            }
            
            if (uiState.errorMessage != null) {
                Text(uiState.errorMessage!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 8.dp))
            }

            Button(
                onClick = { showItemDialog = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.selectedBeneficiarioId != null
            ) {
                Text("ADICIONAR ITEM")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { onScheduleClick(dataAgendamento) },
                enabled = uiState.selectedBeneficiarioId != null && uiState.itensSelecionados.isNotEmpty() && dataAgendamento.isNotBlank() && !uiState.isScheduling,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (uiState.isScheduling) "A AGENDAR..." else "AGENDAR ENTREGA")
            }
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelecionarItemDialog(
    lotes: List<LoteIndividual>,
    onDismiss: () -> Unit,
    onAddItem: (LoteIndividual, Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedLote by remember { mutableStateOf<LoteIndividual?>(null) }
    var quantidade by remember { mutableStateOf("1") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar Item à Entrega") },
        text = {
            Column {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = selectedLote?.produto ?: "",
                        onValueChange = {}, 
                        readOnly = true,
                        label = { Text("Selecionar Lote") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        lotes.forEach { lote ->
                            DropdownMenuItem(
                                text = { Text("${lote.produto} (Qtd: ${lote.quantidadeAtual}, Val: ${lote.dataValidade ?: "N/A"})") },
                                onClick = {
                                    selectedLote = lote
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = quantidade,
                    onValueChange = { quantidade = it },
                    label = { Text("Quantidade a Entregar") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qtd = quantidade.toIntOrNull() ?: 0
                    val stockDisponivel = selectedLote?.quantidadeAtual ?: 0
                    if (selectedLote != null && qtd > 0 && qtd <= stockDisponivel) {
                        onAddItem(selectedLote!!, qtd)
                    } else {
                        android.widget.Toast.makeText(context, "Quantidade inválida ou superior ao stock.", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = selectedLote != null && (quantidade.toIntOrNull() ?: 0) > 0
            ) { Text("ADICIONAR") }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("CANCELAR") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeneficiarioSelector(
    beneficiarios: List<Beneficiario>,
    onBeneficiarioSelected: (Beneficiario) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedBeneficiario by remember { mutableStateOf<Beneficiario?>(null) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = modifier) {
        OutlinedTextField(
            value = selectedBeneficiario?.nomeCompleto ?: "",
            onValueChange = {}, 
            readOnly = true,
            label = { Text("Selecione um Beneficiário") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            beneficiarios.forEach { beneficiario ->
                DropdownMenuItem(
                    text = { Text(text = "${beneficiario.nomeCompleto} (${beneficiario.numEstudante ?: "N/A"})") },
                    onClick = {
                        selectedBeneficiario = beneficiario
                        onBeneficiarioSelected(beneficiario)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun DatePickerField(
    date: String, 
    onDateSelected: (String) -> Unit, 
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Surface(
        modifier = modifier.clickable { showDatePicker(context, onDateSelected) },
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (date.isEmpty()) "Data de Agendamento" else date,
                modifier = Modifier.weight(1f),
                color = if (date.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Selecionar Data",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun showDatePicker(context: android.content.Context, onDateSelected: (String) -> Unit) {
    val calendar = java.util.Calendar.getInstance()
    android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth -> onDateSelected(String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)) },
        calendar.get(java.util.Calendar.YEAR),
        calendar.get(java.util.Calendar.MONTH),
        calendar.get(java.util.Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.minDate = System.currentTimeMillis()
    }.show()
}

@Composable
fun ItemEntregaRow(
    item: ItemSelecionado,
    onRemoveClick: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text = item.lote.produto, modifier = Modifier.weight(1f))
        Text(text = "Qtd: ${item.quantidade}", modifier = Modifier.padding(horizontal = 8.dp))
        IconButton(onClick = onRemoveClick) {
            Icon(Icons.Default.Delete, "Remover Item", tint = MaterialTheme.colorScheme.error)
        }
    }
}
