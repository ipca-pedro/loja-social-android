package com.example.loja_social.ui.beneficiarios

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
fun BeneficiariosScreen(viewModel: BeneficiariosViewModel, onNavigateToDetail: (String?) -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Beneficiários") }) },
        floatingActionButton = { 
            FloatingActionButton(onClick = { onNavigateToDetail(null) }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Beneficiário")
            }
        }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .padding(16.dp)) {
            
            SearchAndFilter(viewModel)
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
                uiState.isEmpty() -> {
                    Text("Nenhum beneficiário encontrado.")
                }
                else -> {
                    LazyColumn {
                        items(uiState) { beneficiario ->
                            BeneficiarioRow(beneficiario) { 
                                onNavigateToDetail(beneficiario.id.toString()) 
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SearchAndFilter(viewModel: BeneficiariosViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<String?>(null) }

    Column {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { 
                searchQuery = it
                viewModel.setSearchQuery(it)
            },
            label = { Text("Pesquisar...") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selectedFilter == null,
                onClick = { 
                    selectedFilter = null
                    viewModel.setFilterState(null)
                }
            ) {
                Text("Todos")
            }
            FilterChip(
                selected = selectedFilter == "ativo",
                onClick = { 
                    selectedFilter = "ativo"
                    viewModel.setFilterState("ativo")
                }
            ) {
                Text("Ativo")
            }
            FilterChip(
                selected = selectedFilter == "inativo",
                onClick = { 
                    selectedFilter = "inativo"
                    viewModel.setFilterState("inativo")
                }
            ) {
                Text("Inativo")
            }
        }
    }
}

@Composable
fun BeneficiarioRow(beneficiario: com.example.loja_social.api.Beneficiario, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        elevation = 2.dp
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(beneficiario.nomeCompleto, fontWeight = FontWeight.Bold)
                Text(beneficiario.numEstudante ?: "", color = Color.Gray)
            }
            Text(
                beneficiario.estado ?: "", 
                color = if (beneficiario.estado == "ativo") Color.Green else Color.Red,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

