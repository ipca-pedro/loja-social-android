
package com.example.loja_social.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.loja_social.SessionManager
import com.example.loja_social.api.RetrofitInstance
import com.example.loja_social.repository.LoginRepository
import com.example.loja_social.ui.main.MainActivity
import com.example.loja_social.ui.theme.LojaSocialTheme
import kotlinx.coroutines.launch

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
        if (sessionManager.fetchAuthToken() != null) {
            navigateToMain()
            return
        }

        setContent {
            LojaSocialTheme {
                LoginScreen(viewModel)
            }
        }

        // Observa o evento de sucesso para navegar
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (state is LoginUiState.Success) {
                        navigateToMain()
                    }
                }
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

@Composable
fun LoginScreen(viewModel: LoginViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("admin@lojasocial.pt") }
    var password by remember { mutableStateOf("password123") }

    val isLoading = uiState is LoginUiState.Loading

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Bem-Vindo", style = MaterialTheme.typography.h4)
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (uiState is LoginUiState.Error) {
                Text(
                    text = (uiState as LoginUiState.Error).message,
                    color = MaterialTheme.colors.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = { viewModel.login(email, password) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("LOGIN")
                }
            }
        }
    }
}