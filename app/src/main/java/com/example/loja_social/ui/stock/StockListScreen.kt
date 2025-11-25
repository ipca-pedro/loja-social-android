package com.example.loja_social.ui.stock

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun StockListScreen(
    viewModel: StockListViewModel,
    onNavigateToDetail: (com.example.loja_social.api.StockItem) -> Unit,
    onNavigateToAdd: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Stock") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAdd) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Stock")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            StockFilters(uiState.categories) { type, category ->
                viewModel.setFilterType(type)
                viewModel.setCategoryFilter(category)
            }
            Spacer(modifier = Modifier.height(16.dp))

            when {
                isLoading -> {
                     Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                errorMessage != null -> {
                    Text(errorMessage!!, color = MaterialTheme.colors.error)
                }
                uiState.stockItems.isEmpty() -> {
                    Text("Nenhum item em stock encontrado.")
                }
                else -> {
                    LazyColumn {
                        items(uiState.stockItems) { stockItem ->
                            StockItemRow(stockItem, onClick = { onNavigateToDetail(stockItem) })
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun StockFilters(categories: List<String>, onFilterChanged: (String?, String?) -> Unit) {
    var filterType by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = filterType == null, onClick = { filterType = null; onFilterChanged(null, selectedCategory) }) { Text("Todos") }
            FilterChip(selected = filterType == "validade_proxima", onClick = { filterType = "validade_proxima"; onFilterChanged("validade_proxima", selectedCategory) }) { Text("Validade Próxima") }
            FilterChip(selected = filterType == "stock_baixo", onClick = { filterType = "stock_baixo"; onFilterChanged("stock_baixo", selectedCategory) }) { Text("Stock Baixo") }
        }
        Spacer(modifier = Modifier.height(8.dp))
        ExposedDropdownMenuBox(expanded = false, onExpandedChange = {}) {
            OutlinedTextField(
                value = selectedCategory ?: "Todas as categorias",
                onValueChange = {},
                readOnly = true,
                label = { Text("Filtrar por Categoria") },
                modifier = Modifier.fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = false, onDismissRequest = {}) {
                DropdownMenuItem(onClick = { selectedCategory = null; onFilterChanged(filterType, null)}) { Text("Todas as categorias") }
                categories.forEach {
                    DropdownMenuItem(onClick = { selectedCategory = it; onFilterChanged(filterType, it)}) { Text(it) }
                }
            }
        }
    }
}

@Composable
fun StockItemRow(stockItem: com.example.loja_social.api.StockItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable(onClick = onClick),
        elevation = 2.dp
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stockItem.produto, fontWeight = FontWeight.Bold)
                Text("Qtd: ${stockItem.quantidadeTotal} | Lotes: ${stockItem.lotes}", style = MaterialTheme.typography.body2)
            }
            stockItem.validadeProxima?.let {
                Text("Validade: $it", color = Color.Red, style = MaterialTheme.typography.caption)
            }
        }
    }
}

