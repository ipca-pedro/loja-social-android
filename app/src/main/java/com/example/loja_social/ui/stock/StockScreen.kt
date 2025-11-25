package com.example.loja_social.ui.stock

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.loja_social.api.Produto
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun StockScreen(viewModel: StockViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedProduct by remember { mutableStateOf<Produto?>(null) }
    var quantidade by remember { mutableStateOf("") }
    var dataValidade by remember { mutableStateOf("") }

    val productsInCategory = uiState.produtos.filter { it.categoria == selectedCategory }

    // Limpa o formulário quando uma mensagem de sucesso é mostrada
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            selectedCategory = null
            selectedProduct = null
            quantidade = ""
            dataValidade = ""
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Adicionar Stock") }) }) {
        Box(modifier = Modifier.padding(it).fillMaxSize()){
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Categoria Dropdown
                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = { }
                ) {
                    OutlinedTextField(
                        value = selectedCategory ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoria") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = false, onDismissRequest = { }) {
                        uiState.categorias.forEach { category ->
                            DropdownMenuItem(onClick = {
                                selectedCategory = category.nome
                                selectedProduct = null
                            }) {
                                Text(category.nome)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Produto Dropdown
                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = { }
                ) {
                    OutlinedTextField(
                        value = selectedProduct?.nome ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Produto") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedCategory != null
                    )
                    ExposedDropdownMenu(expanded = false, onDismissRequest = {}) {
                        productsInCategory.forEach { product ->
                            DropdownMenuItem(onClick = { selectedProduct = product }) {
                                Text(product.nome)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = quantidade,
                    onValueChange = { quantidade = it },
                    label = { Text("Quantidade") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = dataValidade, 
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Data de Validade (Opcional)") },
                    modifier = Modifier.fillMaxWidth().clickable { 
                        showDatePickerDialog(context, dataValidade) { dataValidade = it }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Mensagens e Botão
                if (uiState.errorMessage != null) {
                    Text(uiState.errorMessage!!, color = MaterialTheme.colors.error)
                }
                if (uiState.successMessage != null) {
                    Text(uiState.successMessage!!, color = Color(0xFF2E7D32))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        selectedProduct?.let {
                            viewModel.addStockItem(it.id, quantidade, dataValidade)
                        }
                    },
                    enabled = selectedProduct != null && !uiState.isFormLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (uiState.isFormLoading) "A ADICIONAR..." else "ADICIONAR STOCK")
                }
            }
            if(uiState.isLoading){
                 CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

private fun showDatePickerDialog(context: android.content.Context, initialDate: String, onDateSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    try {
        if (initialDate.isNotEmpty()) {
            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            calendar.time = sdf.parse(initialDate)!!
        }
    } catch (e: Exception) { }

    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            onDateSelected(String.format("%02d/%02d/%d", dayOfMonth, month + 1, year))
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.minDate = System.currentTimeMillis()
    }.show()
}

