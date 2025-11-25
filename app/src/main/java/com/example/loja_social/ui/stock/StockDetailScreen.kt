package com.example.loja_social.ui.stock

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.loja_social.api.LoteIndividual
import com.example.loja_social.api.StockItem
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StockDetailScreen(viewModel: StockDetailViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var showEditDialog by remember { mutableStateOf<LoteIndividual?>(null) }
    var showDeleteDialog by remember { mutableStateOf<LoteIndividual?>(null) }

    Scaffold(topBar = { TopAppBar(title = { Text("Detalhes do Stock") }) }) { paddingValues ->
        Box(modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()) {
            when {
                uiState.isLoading && uiState.stockItem == null -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.errorMessage != null -> {
                    Text(
                        text = uiState.errorMessage!!,
                        color = MaterialTheme.colors.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.stockItem != null -> {
                    Column(modifier = Modifier.padding(16.dp)) {
                        StockItemDetails(uiState.stockItem!!)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Lotes Individuais", style = MaterialTheme.typography.h6)
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        LazyColumn {
                            items(uiState.lotes) {
                                LoteItemRow(it, onEditClick = { showEditDialog = it }, onDeleteClick = { showDeleteDialog = it })
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditDialog != null) {
        EditLoteDialog(
            lote = showEditDialog!!,
            onDismiss = { showEditDialog = null },
            onSave = { loteId, quantidade, data ->
                viewModel.updateLote(loteId.toString(), quantidade, data)
                showEditDialog = null
            }
        )
    }

    if (showDeleteDialog != null) {
        DeleteLoteDialog(
            lote = showDeleteDialog!!,
            onDismiss = { showDeleteDialog = null },
            onConfirm = {
                viewModel.deleteLote(it.id)
                showDeleteDialog = null
            }
        )
    }
}

@Composable
fun StockItemDetails(stockItem: StockItem) {
    Column {
        Text(stockItem.produto, style = MaterialTheme.typography.h5)
        Text("Categoria: ${stockItem.categoria ?: "N/A"}", style = MaterialTheme.typography.body1)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${stockItem.quantidadeTotal}", style = MaterialTheme.typography.h6)
                Text("Unidades", style = MaterialTheme.typography.caption)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${stockItem.lotes}", style = MaterialTheme.typography.h6)
                Text(if (stockItem.lotes == 1) "Lote" else "Lotes", style = MaterialTheme.typography.caption)
            }
        }
        stockItem.validadeProxima?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Validade Próxima: $it", style = MaterialTheme.typography.body2, color = Color.Red)
        }
    }
}

@Composable
fun LoteItemRow(lote: LoteIndividual, onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp), elevation = 2.dp) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Lote #${lote.id}", fontWeight = FontWeight.Bold)
                Text("Quantidade: ${lote.quantidadeAtual}", fontSize = 14.sp)
                lote.dataValidade?.let {
                    Text("Validade: $it", fontSize = 14.sp)
                }
            }
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = "Editar Lote")
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "Remover Lote", tint = MaterialTheme.colors.error)
            }
        }
    }
}

@Composable
fun EditLoteDialog(lote: LoteIndividual, onDismiss: () -> Unit, onSave: (String, Int, String?) -> Unit) {
    var quantidade by remember { mutableStateOf(lote.quantidadeAtual.toString()) }
    var dataValidade by remember { mutableStateOf(lote.dataValidade ?: "") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Lote") },
        text = {
            Column {
                OutlinedTextField(
                    value = quantidade,
                    onValueChange = { quantidade = it },
                    label = { Text("Quantidade Atual") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = dataValidade,
                    onValueChange = { dataValidade = it },
                    label = { Text("Data de Validade (yyyy-MM-dd)") },
                    readOnly = true,
                    modifier = Modifier.clickable {
                        showDatePickerDialog(context, dataValidade) { dataValidade = it }
                    }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val qtd = quantidade.toIntOrNull()
                if (qtd == null || qtd < 0 || qtd > lote.quantidadeInicial) {
                    Toast.makeText(context, "Quantidade inválida.", Toast.LENGTH_SHORT).show()
                } else {
                    onSave(lote.id, qtd, dataValidade.ifEmpty { null })
                }
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun DeleteLoteDialog(lote: LoteIndividual, onDismiss: () -> Unit, onConfirm: (LoteIndividual) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmar Remoção") },
        text = { Text("Tem a certeza que deseja remover este lote? A ação não pode ser desfeita.") },
        confirmButton = {
            Button(onClick = { onConfirm(lote) }) { Text("Remover") }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

private fun showDatePickerDialog(context: android.content.Context, initialDate: String, onDateSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    try {
        if (initialDate.isNotEmpty()) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            calendar.time = sdf.parse(initialDate)!!
        }
    } catch (e: Exception) { }

    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            onDateSelected(String.format("%d-%02d-%02d", year, month + 1, dayOfMonth))
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}
