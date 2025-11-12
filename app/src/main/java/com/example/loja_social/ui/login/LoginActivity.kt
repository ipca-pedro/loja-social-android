package com.example.loja_social.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.loja_social.SessionManager
import com.example.loja_social.api.RetrofitInstance
import com.example.loja_social.databinding.ActivityLoginBinding
import com.example.loja_social.repository.LoginRepository
import com.example.loja_social.ui.main.MainActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val viewModel: LoginViewModel by viewModels {
        val apiService = RetrofitInstance.api
        val sessionManager = SessionManager(applicationContext)
        val loginRepository = LoginRepository(apiService)
        LoginViewModelFactory(loginRepository, sessionManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- CORREÇÃO ADICIONADA AQUI ---
        // 1. Verificar se já existe um token ANTES de mostrar o layout
        val sessionManager = SessionManager(applicationContext)
        if (sessionManager.fetchAuthToken() != null) {
            // 2. Se sim, saltar diretamente para a MainActivity
            navigateToMain()
            return // 3. Importante: não continuar a executar o resto do onCreate
        }
        // --- FIM DA CORREÇÃO ---

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etEmail.setText("admin@lojasocial.pt")
        binding.etPassword.setText("password123")

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            viewModel.login(email, password)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    handleUiState(state)
                }
            }
        }
    }

    private fun handleUiState(state: LoginUiState) {
        when (state) {
            is LoginUiState.Idle -> {
                binding.progressBar.visibility = View.GONE
                binding.btnLogin.isEnabled = true
                binding.tvErrorMessage.visibility = View.GONE
            }
            is LoginUiState.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
                binding.btnLogin.isEnabled = false
                binding.tvErrorMessage.visibility = View.GONE
            }
            is LoginUiState.Success -> {
                binding.progressBar.visibility = View.GONE
                navigateToMain()
            }
            is LoginUiState.Error -> {
                binding.progressBar.visibility = View.GONE
                binding.btnLogin.isEnabled = true
                binding.tvErrorMessage.text = state.message
                binding.tvErrorMessage.visibility = View.VISIBLE
            }
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
        finish() // 'finish()' remove a LoginActivity da pilha, não podes voltar atrás
    }
}