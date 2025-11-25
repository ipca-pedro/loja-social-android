package com.example.loja_social.ui.entregas

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.loja_social.api.Beneficiario
import com.example.loja_social.api.LoteIndividual
import java.util.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AgendarEntregaScreen(
    viewModel: AgendarEntregaViewModel,
    onScheduleClick: (String) -> Unit,
    onNavigateBack: () -> Unit = {}
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
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                    dataAgendamento = ""
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

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Agendar Nova Entrega") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                        }
                    }
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
                    onBeneficiarioSelected = { viewModel.onBeneficiarioSelected(it) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                DatePickerField(
                    date = dataAgendamento,
                    onDateSelected = { dataAgendamento = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Itens para Entrega", style = MaterialTheme.typography.h6)
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(uiState.itensSelecionados) { item ->
                        ItemEntregaRow(
                            item = item.lote,
                            onRemoveClick = { viewModel.removerItem(item.lote.id) },
                            onQuantityChanged = { novaQuantidade ->
                                viewModel.atualizarQuantidade(item.lote.id, novaQuantidade)
                            }
                        )
                    }
                }

                if (uiState.errorMessage != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        backgroundColor = Color(0xFFFFF0F0)
                    ) {
                        Text(
                            text = uiState.errorMessage ?: "",
                            modifier = Modifier.padding(16.dp),
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Button(
                    onClick = { showItemDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ADICIONAR ITEM")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onScheduleClick(dataAgendamento) },
                    enabled = uiState.selectedBeneficiarioId != null && !uiState.isScheduling,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (uiState.isScheduling) "A AGENDAR..." else "AGENDAR ENTREGA")
                }
            }
        }

        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SelecionarItemDialog(
    lotes: List<LoteIndividual>,
    onDismiss: () -> Unit,
    onAddItem: (LoteIndividual, Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedLote by remember { mutableStateOf<LoteIndividual?>(null) }
    var quantidade by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar Item à Entrega") },
        text = {
            Column {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedLote?.produto ?: "",
                        onValueChange = {}, 
                        readOnly = true,
                        label = { Text("Selecionar Lote de Produto") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        lotes.forEach { lote ->
                            DropdownMenuItem(onClick = {
                                selectedLote = lote
                                expanded = false
                            }) {
                                Text(text = "${lote.produto} (Qtd: ${lote.quantidadeAtual})")
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = quantidade,
                    onValueChange = { quantidade = it },
                    label = { Text("Quantidade a Entregar") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qtd = quantidade.toIntOrNull() ?: 0
                    if (selectedLote != null && qtd > 0) {
                        onAddItem(selectedLote!!, qtd)
                    }
                },
                enabled = selectedLote != null && (quantidade.toIntOrNull() ?: 0) > 0
            ) {
                Text("ADICIONAR")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("CANCELAR")
            }
        }
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BeneficiarioSelector(
    beneficiarios: List<Beneficiario>,
    onBeneficiarioSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf(TextFieldValue("")) }
    val beneficiarioStrings = beneficiarios.map { "${it.nomeCompleto} (${it.numEstudante ?: "N/A"})" }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = {},
            readOnly = true,
            label = { Text("Selecione um Beneficiário") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            beneficiarioStrings.forEach { beneficiario ->
                DropdownMenuItem(onClick = {
                    selectedText = TextFieldValue(beneficiario)
                    onBeneficiarioSelected(beneficiario)
                    expanded = false
                }) {
                    Text(text = beneficiario)
                }
            }
        }
    }
}

@Composable
fun DatePickerField(date: String, onDateSelected: (String) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    OutlinedTextField(
        value = date,
        onValueChange = {},
        readOnly = true,
        label = { Text("Data de Agendamento") },
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        onDateSelected(String.format("%02d/%02d/%d", dayOfMonth, month + 1, year))
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).apply {
                    datePicker.minDate = System.currentTimeMillis() - 1000
                }.show()
            }
    )
}

@Composable
fun ItemEntregaRow(
    item: LoteIndividual,
    onRemoveClick: () -> Unit,
    onQuantityChanged: (Int) -> Unit
) {
    var quantity by remember { mutableStateOf(item.quantidadeAtual.toString()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = item.produto, modifier = Modifier.weight(1f))
        OutlinedTextField(
            value = quantity,
            onValueChange = {
                quantity = it
                it.toIntOrNull()?.let(onQuantityChanged)
            },
            modifier = Modifier.width(80.dp),
            label = { Text("Qtd.") }
        )
        IconButton(onClick = onRemoveClick) {
            Icon(Icons.Default.Delete, contentDescription = "Remover Item", tint = MaterialTheme.colors.error)
        }
    }
}

