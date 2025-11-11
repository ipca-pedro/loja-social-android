package com.example.loja_social.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.loja_social.MainActivity
import com.example.loja_social.data.api.RetrofitInstance
import com.example.loja_social.data.repository.LoginRepository
import com.example.loja_social.data.session.SessionManager
import com.example.loja_social.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    // Usar ViewBinding para aceder às views do XML de forma segura
    private lateinit var binding: ActivityLoginBinding

    // 1. INICIALIZAR O VIEWMODEL
    // O "by viewModels" gere o ciclo de vida do ViewModel por nós.
    // Passamos a nossa Factory para que o Android saiba como criar o ViewModel
    // com as dependências (Repository e SessionManager).
    private val viewModel: LoginViewModel by viewModels {
        // "Injeção de dependência" manual
        val apiService = RetrofitInstance.api
        val sessionManager = SessionManager(applicationContext)
        val loginRepository = LoginRepository(apiService)
        LoginViewModelFactory(loginRepository, sessionManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflar o layout usando ViewBinding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Preencher os dados de teste (como tinhas antes)
        binding.etEmail.setText("admin@lojaipca.pt") // Corrigi o email para o que está no relatório
        binding.etPassword.setText("password123")

        // 2. CONFIGURAR O CLICK LISTENER
        // Agora, o botão apenas "avisa" o ViewModel para tentar fazer login.
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            viewModel.login(email, password)
        }

        // 3. OBSERVAR O ESTADO DA UI
        // A Activity "ouve" as mudanças de estado vindas do ViewModel
        // e reage, atualizando a UI.
        lifecycleScope.launch {
            // "repeatOnLifecycle" garante que este bloco só corre quando a Activity está visível
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    handleUiState(state)
                }
            }
        }
    }

    /**
     * Função "burra" que apenas atualiza a UI com base no estado
     * recebido do ViewModel.
     */
    private fun handleUiState(state: LoginUiState) {
        when (state) {
            is LoginUiState.Idle -> {
                // Estado inicial ou resetado
                binding.progressBar.visibility = View.GONE
                binding.btnLogin.isEnabled = true
                binding.tvErrorMessage.visibility = View.GONE
            }
            is LoginUiState.Loading -> {
                // A carregar
                binding.progressBar.visibility = View.VISIBLE
                binding.btnLogin.isEnabled = false
                binding.tvErrorMessage.visibility = View.GONE
            }
            is LoginUiState.Success -> {
                // Sucesso!
                binding.progressBar.visibility = View.GONE
                // O ViewModel já guardou o token, só precisamos de navegar.
                navigateToMain()
            }
            is LoginUiState.Error -> {
                // Erro
                binding.progressBar.visibility = View.GONE
                binding.btnLogin.isEnabled = true
                binding.tvErrorMessage.text = state.message
                binding.tvErrorMessage.visibility = View.VISIBLE
                // Opcional: resetar o estado para idle se o utilizador
                // começar a escrever outra vez
                // viewModel.resetStateToIdle()
            }
        }
    }

    /**
     * Navega para a MainActivity e fecha a LoginActivity.
     */
    private fun navigateToMain() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
        finish() // Fecha a LoginActivity para que o utilizador não possa voltar atrás
    }

    //
    // NOTA: A função "performLogin()" antiga foi completamente REMOVIDA.
    // A lógica dela está agora dividida entre o setOnClickListener e o handleUiState.
    //
}