package com.example.loja_social.ui.stock

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.loja_social.api.Produto
import com.example.loja_social.ui.components.LoadingState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.Alignment


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockScreen(viewModel: StockViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    var produtoSelecionado by remember { mutableStateOf<Produto?>(null) }
    var quantidade by remember { mutableStateOf("") }
    var dataValidade by remember { mutableStateOf("") }
    var campanhaSelecionada by remember { mutableStateOf<com.example.loja_social.api.Campanha?>(null) }
    
    var produtoExpanded by remember { mutableStateOf(false) }
    var campanhaExpanded by remember { mutableStateOf(false) }
    var showCreateProductDialog by remember { mutableStateOf(false) }

    if (showCreateProductDialog) {
        CreateProductDialog(
            categorias = uiState.categorias,
            onDismiss = { showCreateProductDialog = false },
            onConfirm = { nome, descricao, categoriaId ->
                viewModel.createProduct(nome, descricao, categoriaId)
                showCreateProductDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Adicionar Stock") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Dropdown para Produtos com botão de adicionar
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    ExposedDropdownMenuBox(
                        expanded = produtoExpanded,
                        onExpandedChange = { produtoExpanded = !produtoExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = produtoSelecionado?.nome ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Produto") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = produtoExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = produtoExpanded,
                            onDismissRequest = { produtoExpanded = false }
                        ) {
                            uiState.produtos.forEach { produto ->
                                DropdownMenuItem(
                                    text = { Text(produto.nome) },
                                    onClick = {
                                        produtoSelecionado = produto
                                        produtoExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                IconButton(
                    onClick = { showCreateProductDialog = true },
                    modifier = Modifier.padding(top = 6.dp) // Alinhamento visual
                ) {
                    Icon(androidx.compose.material.icons.Icons.Default.Add, contentDescription = "Criar Novo Produto")
                }
            }

            OutlinedTextField(
                value = quantidade,
                onValueChange = { quantidade = it },
                label = { Text("Quantidade") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = dataValidade,
                onValueChange = { dataValidade = it },
                label = { Text("Data de Validade (DD/MM/AAAA)") },
                placeholder = { Text("Opcional") },
                modifier = Modifier.fillMaxWidth()
            )

            // Dropdown para Campanhas
            ExposedDropdownMenuBox(
                expanded = campanhaExpanded,
                onExpandedChange = { campanhaExpanded = !campanhaExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = campanhaSelecionada?.nome ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Campanha (Opcional)") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = campanhaExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = campanhaExpanded,
                    onDismissRequest = { campanhaExpanded = false }
                ) {
                    uiState.campanhas.forEach { campanha ->
                        DropdownMenuItem(
                            text = { Text(campanha.nome) },
                            onClick = {
                                campanhaSelecionada = campanha
                                campanhaExpanded = false
                            }
                        )
                    }
                }
            }

            if (uiState.isLoading) {
                LoadingState()
            }

            uiState.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
            
            uiState.successMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
            }

            Button(
                onClick = {
                    produtoSelecionado?.let {
                        viewModel.addStockItem(
                            produtoId = it.id,
                            quantidade = quantidade,
                            dataValidade = dataValidade,
                            campanhaId = campanhaSelecionada?.id
                        )
                    }
                },
                enabled = !uiState.isFormLoading && produtoSelecionado != null && quantidade.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isFormLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text("ADICIONAR STOCK")
                }
            }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProductDialog(
    categorias: List<com.example.loja_social.api.Categoria>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int) -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var categoriaSelecionada by remember { mutableStateOf<com.example.loja_social.api.Categoria?>(categorias.firstOrNull()) }
    var expanded by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Produto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome do Produto") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = descricao,
                    onValueChange = { descricao = it },
                    label = { Text("Descrição (Opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = categoriaSelecionada?.nome ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoria") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categorias.forEach { categoria ->
                            DropdownMenuItem(
                                text = { Text(categoria.nome) },
                                onClick = {
                                    categoriaSelecionada = categoria
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                if (showError) {
                    Text("Preencha o nome e selecione uma categoria.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nome.isNotBlank() && categoriaSelecionada != null) {
                        onConfirm(nome, descricao, categoriaSelecionada!!.id)
                    } else {
                        showError = true
                    }
                }
            ) {
                Text("CRIAR")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR")
            }
        }
    )
}