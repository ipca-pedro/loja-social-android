package com.example.loja_social.ui.stock

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.loja_social.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockListScreen(
    viewModel: StockListViewModel,
    onNavigateToDetail: (com.example.loja_social.api.StockItem) -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stock") },
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
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Stock")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StockFilters(uiState.categories) { type, category ->
                viewModel.setFilterType(type)
                viewModel.setCategoryFilter(category)
            }

            when {
                isLoading -> LoadingState()
                errorMessage != null -> {
                    Text(
                        errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                uiState.stockItems.isEmpty() -> {
                    EmptyState(
                        title = "Nenhum item em stock",
                        subtitle = "Adicione o primeiro item usando o botão +",
                        icon = Icons.Default.Inventory
                    )
                }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(uiState.stockItems) { stockItem ->
                            LojaSocialListItem(
                                title = stockItem.produto,
                                subtitle = "Qtd: ${stockItem.quantidadeTotal} | Lotes: ${stockItem.lotes}",
                                trailing = stockItem.validadeProxima?.let { "Validade: ${it.substringBefore("T")}" },
                                onClick = { onNavigateToDetail(stockItem) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockFilters(categories: List<String>, onFilterChanged: (String?, String?) -> Unit) {
    var filterType by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = filterType == null,
                onClick = { filterType = null; onFilterChanged(null, selectedCategory) },
                label = { Text("Todos") }
            )
            FilterChip(
                selected = filterType == "validade_proxima",
                onClick = { filterType = "validade_proxima"; onFilterChanged("validade_proxima", selectedCategory) },
                label = { Text("Validade Próxima") }
            )
            FilterChip(
                selected = filterType == "stock_baixo",
                onClick = { filterType = "stock_baixo"; onFilterChanged("stock_baixo", selectedCategory) },
                label = { Text("Stock Baixo") }
            )
        }
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedCategory ?: "Todas as categorias",
                onValueChange = {},
                readOnly = true,
                label = { Text("Filtrar por Categoria") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Todas as categorias") },
                    onClick = {
                        selectedCategory = null
                        onFilterChanged(filterType, null)
                        expanded = false
                    }
                )
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            selectedCategory = category
                            onFilterChanged(filterType, category)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}



