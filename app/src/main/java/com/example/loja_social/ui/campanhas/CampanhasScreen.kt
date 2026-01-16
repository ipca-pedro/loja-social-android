package com.example.loja_social.ui.campanhas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.loja_social.ui.components.LoadingState
import com.example.loja_social.ui.components.EmptyState
import com.example.loja_social.ui.components.StatusChip
import com.example.loja_social.api.Campanha

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampanhasScreen(
    viewModel: CampanhasViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var campanhaToEdit by remember { mutableStateOf<Campanha?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf<Campanha?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestão de Campanhas") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    campanhaToEdit = null
                    showDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nova Campanha")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {

                if (uiState.isLoading) {
                    LoadingState()
                } else if (uiState.campanhas.isEmpty()) {
                    EmptyState(
                        title = "Sem Campanhas",
                        subtitle = "Não existem campanhas registadas.",
                        icon = Icons.Default.Event
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.campanhas) { campanha ->
                            CampanhaItem(
                                campanha = campanha,
                                onEditClick = {
                                    campanhaToEdit = campanha
                                    showDialog = true
                                },
                                onDeleteClick = {
                                    showDeleteConfirmDialog = campanha
                                }
                            )
                        }
                    }
                }
            }

            // Error Message
            uiState.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
                LaunchedEffect(error) {
                    kotlinx.coroutines.delay(3000)
                    viewModel.clearMessages()
                }
            }
            
            // Success Message
            uiState.successMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
                LaunchedEffect(message) {
                    kotlinx.coroutines.delay(3000)
                    viewModel.clearMessages()
                }
            }
        }
    }

    if (showDialog) {
        CampanhaDialog(
            campanha = campanhaToEdit,
            onDismiss = { showDialog = false },
            onConfirm = { nome, descricao, inicio, fim, ativo, tipo ->
                if (campanhaToEdit != null) {
                    viewModel.updateCampanha(campanhaToEdit!!.id, nome, descricao, inicio, fim, ativo, tipo)
                } else {
                    viewModel.createCampanha(nome, descricao, inicio, fim, tipo)
                }
                showDialog = false
            }
        )
    }

    if (showDeleteConfirmDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text("Apagar Campanha") },
            text = { Text("Tem a certeza que deseja apagar a campanha '${showDeleteConfirmDialog?.nome}'?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirmDialog?.let { viewModel.deleteCampanha(it.id) }
                    showDeleteConfirmDialog = null
                }) {
                    Text("Apagar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun CampanhaItem(
    campanha: Campanha,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = campanha.nome,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
             
             Spacer(modifier = Modifier.height(4.dp))
             
             Row(
                verticalAlignment = Alignment.CenterVertically
             ) {
                 StatusChip(status = if (campanha.ativo != false) "Ativo" else "Inativo")
                 Spacer(modifier = Modifier.width(8.dp))
                 SuggestionChip(
                    onClick = { },
                    label = { Text(campanha.tipo) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                 )
             }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (campanha.descricao != null) {
                Text(
                    text = campanha.descricao,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            Text(
                text = "De ${campanha.dataInicio.take(10)} até ${campanha.dataFim.take(10)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Editar")
                }
                TextButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Apagar", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampanhaDialog(
    campanha: Campanha?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, Boolean, String) -> Unit
) {
    var nome by remember { mutableStateOf(campanha?.nome ?: "") }
    var descricao by remember { mutableStateOf(campanha?.descricao ?: "") }
    var dataInicio by remember { mutableStateOf(campanha?.dataInicio?.take(10) ?: "") }
    var dataFim by remember { mutableStateOf(campanha?.dataFim?.take(10) ?: "") }
    var ativo by remember { mutableStateOf(campanha?.ativo ?: true) }
    var tipo by remember { mutableStateOf(campanha?.tipo ?: "Interna") }
    var expanded by remember { mutableStateOf(false) }
    val tipos = listOf("Interna", "Parceria Externa", "Doação Privada")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (campanha != null) "Editar Campanha" else "Nova Campanha",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome da Campanha") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = tipo,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Tipo de Campanha") },
                        trailingIcon = { 
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Selecionar Tipo"
                            ) 
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Invisible interaction source to trigger dropdown
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { expanded = true }
                    )
                    
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        tipos.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    tipo = selectionOption
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = descricao,
                    onValueChange = { descricao = it },
                    label = { Text("Descrição") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dataInicio,
                    onValueChange = { dataInicio = it },
                    label = { Text("Data Início (YYYY-MM-DD)") },
                    placeholder = { Text("2024-01-01") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dataFim,
                    onValueChange = { dataFim = it },
                    label = { Text("Data Fim (YYYY-MM-DD)") },
                    placeholder = { Text("2024-12-31") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (campanha != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = ativo,
                            onCheckedChange = { ativo = it }
                        )
                        Text("Campanha Ativa")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = { onConfirm(nome, descricao, dataInicio, dataFim, ativo, tipo) },
                        enabled = nome.isNotEmpty() && dataInicio.isNotEmpty() && dataFim.isNotEmpty()
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}
