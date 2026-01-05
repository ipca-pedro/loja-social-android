package com.example.loja_social.ui.beneficiarios

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.loja_social.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeneficiariosScreen(
    viewModel: BeneficiariosViewModel, 
    onNavigateToDetail: (String?) -> Unit,
    shouldRefresh: Boolean,
    onRefreshDone: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            viewModel.fetchBeneficiarios()
            onRefreshDone()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Beneficiários") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = { 
            FloatingActionButton(
                onClick = { onNavigateToDetail(null) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Beneficiário")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SearchAndFilter(viewModel)

            when {
                isLoading -> LoadingState()
                errorMessage != null -> {
                    Text(
                        errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                uiState.isEmpty() -> {
                    EmptyState(
                        title = "Nenhum beneficiário encontrado",
                        subtitle = "Adicione o primeiro beneficiário usando o botão +",
                        icon = Icons.Default.Person
                    )
                }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(uiState) { beneficiario ->
                            LojaSocialListItem(
                                title = beneficiario.nomeCompleto,
                                subtitle = beneficiario.numEstudante ?: "Sem número de estudante",
                                status = beneficiario.estado,
                                onClick = { onNavigateToDetail(beneficiario.id.toString()) }
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
fun SearchAndFilter(viewModel: BeneficiariosViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { 
                searchQuery = it
                viewModel.setSearchQuery(it)
            },
            label = { Text("Pesquisar beneficiários...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selectedFilter == null,
                onClick = { 
                    selectedFilter = null
                    viewModel.setFilterState(null)
                },
                label = { Text("Todos") }
            )
            FilterChip(
                selected = selectedFilter == "ativo",
                onClick = { 
                    selectedFilter = "ativo"
                    viewModel.setFilterState("ativo")
                },
                label = { Text("Ativo") }
            )
            FilterChip(
                selected = selectedFilter == "inativo",
                onClick = { 
                    selectedFilter = "inativo"
                    viewModel.setFilterState("inativo")
                },
                label = { Text("Inativo") }
            )
        }
    }
}
