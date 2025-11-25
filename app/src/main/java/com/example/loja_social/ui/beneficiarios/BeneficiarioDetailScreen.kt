package com.example.loja_social.ui.beneficiarios

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.loja_social.api.BeneficiarioRequest
import com.example.loja_social.api.RetrofitHelper
import com.example.loja_social.api.RetrofitInstance
import com.example.loja_social.repository.BeneficiarioRepository

@Composable
fun BeneficiarioDetailScreen(
    viewModel: BeneficiarioDetailViewModel,
    title: String,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Observa navegação de volta após sucesso
    LaunchedEffect(Unit) {
        viewModel.navigateBack.collect {
            onNavigateBack()
        }
    }

    // Form fields
    var nomeCompleto by remember { mutableStateOf(uiState.beneficiario?.nomeCompleto ?: "") }
    var numEstudante by remember { mutableStateOf(uiState.beneficiario?.numEstudante ?: "") }
    var nif by remember { mutableStateOf(uiState.beneficiario?.nif ?: "") }
    var anoCurricular by remember { mutableStateOf(uiState.beneficiario?.anoCurricular?.toString() ?: "") }
    var curso by remember { mutableStateOf(uiState.beneficiario?.curso ?: "") }
    var email by remember { mutableStateOf(uiState.beneficiario?.email ?: "") }
    var telefone by remember { mutableStateOf(uiState.beneficiario?.telefone ?: "") }
    var notasAdicionais by remember { mutableStateOf(uiState.beneficiario?.notasAdicionais ?: "") }
    var estado by remember { mutableStateOf(uiState.beneficiario?.estado ?: "ativo") }

    // Atualiza campos quando o beneficiário é carregado
    LaunchedEffect(uiState.beneficiario) {
        uiState.beneficiario?.let {
            nomeCompleto = it.nomeCompleto
            numEstudante = it.numEstudante ?: ""
            nif = it.nif ?: ""
            anoCurricular = it.anoCurricular?.toString() ?: ""
            curso = it.curso ?: ""
            email = it.email ?: ""
            telefone = it.telefone ?: ""
            notasAdicionais = it.notasAdicionais ?: ""
            estado = it.estado
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Mensagens de erro/sucesso
                        uiState.errorMessage?.let {
                            Card(
                                backgroundColor = MaterialTheme.colors.error,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = it,
                                    color = MaterialTheme.colors.onError,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }

                        uiState.successMessage?.let {
                            Card(
                                backgroundColor = Color(0xFF2E7D32),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = it,
                                    color = Color.White,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }

                        // Campos do formulário
                        OutlinedTextField(
                            value = nomeCompleto,
                            onValueChange = { nomeCompleto = it },
                            label = { Text("Nome Completo *") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isSaving
                        )

                        OutlinedTextField(
                            value = numEstudante,
                            onValueChange = { numEstudante = it },
                            label = { Text("Número de Estudante") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isSaving
                        )

                        OutlinedTextField(
                            value = nif,
                            onValueChange = { nif = it },
                            label = { Text("NIF") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isSaving
                        )

                        OutlinedTextField(
                            value = anoCurricular,
                            onValueChange = { anoCurricular = it },
                            label = { Text("Ano Curricular") },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isSaving
                        )

                        OutlinedTextField(
                            value = curso,
                            onValueChange = { curso = it },
                            label = { Text("Curso") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isSaving
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Email
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isSaving
                        )

                        OutlinedTextField(
                            value = telefone,
                            onValueChange = { telefone = it },
                            label = { Text("Telefone") },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isSaving
                        )

                        OutlinedTextField(
                            value = notasAdicionais,
                            onValueChange = { notasAdicionais = it },
                            label = { Text("Notas Adicionais") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            maxLines = 5,
                            enabled = !uiState.isSaving
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Botões de ação
                        Button(
                            onClick = {
                                val request = BeneficiarioRequest(
                                    nomeCompleto = nomeCompleto.trim(),
                                    numEstudante = numEstudante.trim().ifEmpty { null },
                                    nif = nif.trim().ifEmpty { null },
                                    anoCurricular = anoCurricular.trim().toIntOrNull(),
                                    curso = curso.trim().ifEmpty { null },
                                    email = email.trim().ifEmpty { null },
                                    telefone = telefone.trim().ifEmpty { null },
                                    notasAdicionais = notasAdicionais.trim().ifEmpty { null },
                                    estado = estado
                                )
                                viewModel.saveBeneficiario(request)
                            },
                            enabled = nomeCompleto.isNotBlank() && !uiState.isSaving,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (uiState.isSaving) "A GUARDAR..." else "GUARDAR")
                        }

                        // Botão de desativar (apenas em modo edição)
                        if (uiState.beneficiario != null && uiState.beneficiario?.estado == "ativo") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    viewModel.deactivateBeneficiario()
                                },
                                enabled = !uiState.isSaving,
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = MaterialTheme.colors.error
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("DESATIVAR BENEFICIÁRIO")
                            }
                        }
                    }
                }
            }
        }
    }
}

