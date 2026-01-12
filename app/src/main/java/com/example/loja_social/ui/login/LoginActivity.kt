
package com.example.loja_social.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.ui.res.painterResource
import com.example.loja_social.R
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.loja_social.SessionManager
import com.example.loja_social.api.RetrofitInstance
import com.example.loja_social.repository.LoginRepository
import com.example.loja_social.ui.main.MainActivity
import com.example.loja_social.ui.theme.LojaSocialTheme
import kotlinx.coroutines.launch
import android.util.Log

class LoginActivity : ComponentActivity() {

    private val viewModel: LoginViewModel by viewModels {
        val apiService = RetrofitInstance.api
        val sessionManager = SessionManager(applicationContext)
        val loginRepository = LoginRepository(apiService)
        LoginViewModelFactory(loginRepository, sessionManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionManager = SessionManager(applicationContext)
        val token = sessionManager.fetchAuthToken()
        val role = sessionManager.fetchUserRole()
        Log.d("LoginActivity", "Token existente: ${token?.take(20)}..., Role: $role")
        
        if (token != null) {
            Log.d("LoginActivity", "Token encontrado, navegando para MainActivity")
            navigateToMain()
            return
        }

        setContent {
            LojaSocialTheme {
                var showUserTypeSelection by remember { mutableStateOf(true) }
                var selectedUserType by remember { mutableStateOf("") }
                
                if (showUserTypeSelection) {
                    UserTypeSelectionScreen(
                        onAdminClick = {
                            selectedUserType = "admin"
                            showUserTypeSelection = false
                        },
                        onBeneficiarioClick = {
                            selectedUserType = "beneficiario"
                            showUserTypeSelection = false
                        }
                    )
                } else {
                    LoginScreen(
                        viewModel = viewModel,
                        userType = selectedUserType,
                        onBackClick = { showUserTypeSelection = true }
                    )
                }
            }
        }

        // Observa o evento de sucesso para navegar
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (state is LoginUiState.Success) {
                        Log.d("LoginActivity", "Login bem-sucedido, navegando para MainActivity")
                        viewModel.resetStateToIdle() // Limpar estado
                        navigateToMain()
                    }
                }
            }
        }
    }

    private fun navigateToMain() {
        Log.d("LoginActivity", "Navegando para MainActivity...")
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    userType: String,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf(if (userType == "admin") "admin@lojasocial.pt" else "a25005@alunos.ipca.pt") }
    var password by remember { mutableStateOf("") }
    
    // Atualizar placeholder para beneficiário
    val passwordLabel = if (userType == "beneficiario") "NIF" else "Password"

    val isLoading = uiState is LoginUiState.Loading

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(32.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Image(
                painter = painterResource(id = R.drawable.logo_ipca),
                contentDescription = "Logo IPCA",
                modifier = Modifier
                    .height(120.dp)
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(120.dp))
            
            Text(
                text = if (userType == "admin") "Login Colaborador" else "Login Beneficiário",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
                var passwordVisible by remember { mutableStateOf(false) }

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(passwordLabel) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = if (userType == "beneficiario") KeyboardType.Number else KeyboardType.Password),
                    trailingIcon = {
                        val image = if (passwordVisible)
                            androidx.compose.material.icons.Icons.Filled.Visibility
                        else
                            androidx.compose.material.icons.Icons.Filled.VisibilityOff

                        val description = if (passwordVisible) "Hide password" else "Show password"

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = description)
                        }
                    }
                )
            Spacer(modifier = Modifier.height(16.dp))

            if (uiState is LoginUiState.Error) {
                Text(
                    text = (uiState as LoginUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = { viewModel.login(email, password, userType) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("LOGIN")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onBackClick) {
                Text("← Voltar")
            }
        }
    }
}