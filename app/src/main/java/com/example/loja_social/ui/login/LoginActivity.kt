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

/**
 * Activity de login.
 * Gerencia autenticação do utilizador e navegação para MainActivity após login bem-sucedido.
 * Verifica se já existe um token válido e redireciona automaticamente se houver.
 */
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

        // Verifica se já existe um token válido (utilizador já autenticado)
        // Se sim, redireciona diretamente para MainActivity sem mostrar a tela de login
        val sessionManager = SessionManager(applicationContext)
        if (sessionManager.fetchAuthToken() != null) {
            navigateToMain()
            return // Não continua a execução do onCreate
        }

        // Infla o layout e configura a UI
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Preenche campos com valores padrão (apenas para desenvolvimento)
        binding.etEmail.setText("admin@lojasocial.pt")
        binding.etPassword.setText("password123")

        // Configura listener do botão de login
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            viewModel.login(email, password)
        }

        // Observa mudanças no estado do ViewModel
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    handleUiState(state)
                }
            }
        }
    }

    /**
     * Atualiza a UI baseado no estado atual do ViewModel.
     * Gerencia visibilidade de componentes e mensagens de erro.
     * @param state Estado atual do login
     */
    private fun handleUiState(state: LoginUiState) {
        when (state) {
            is LoginUiState.Idle -> {
                binding.progressBar.visibility = View.GONE
                binding.btnLogin.isEnabled = true
                binding.cardError.visibility = View.GONE
            }
            is LoginUiState.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
                binding.btnLogin.isEnabled = false
                binding.cardError.visibility = View.GONE
            }
            is LoginUiState.Success -> {
                binding.progressBar.visibility = View.GONE
                navigateToMain()
            }
            is LoginUiState.Error -> {
                binding.progressBar.visibility = View.GONE
                binding.btnLogin.isEnabled = true
                binding.tvErrorMessage.text = state.message
                binding.cardError.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Navega para a MainActivity após login bem-sucedido.
     * Remove a LoginActivity da pilha para evitar voltar atrás.
     */
    private fun navigateToMain() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
        finish() // Remove a LoginActivity da pilha
    }
}