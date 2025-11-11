package com.example.loja_social

import com.example.loja_social.api.LoginResponse
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.example.loja_social.api.LoginRequest
import com.example.loja_social.api.RetrofitInstance
import com.google.android.material.textfield.TextInputEditText
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private val apiService = RetrofitInstance.api
    private lateinit var sessionManager: SessionManager

    // As Views podem não ser inicializadas se já estivermos logados
    private var etEmail: TextInputEditText? = null
    private var etPassword: TextInputEditText? = null
    private var btnLogin: Button? = null
    private var tvErrorMessage: TextView? = null
    private var progressBar: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- INICIALIZAR O "COFRE" PRIMEIRO ---
        sessionManager = SessionManager(applicationContext)

        // --- A VERIFICAÇÃO QUE FALTAVA ---
        // Se já temos um token guardado, saltamos o login
        if (sessionManager.fetchAuthToken() != null) {
            Log.d("LoginActivity", "Token encontrado! A redirecionar para a MainActivity.")
            redirectToMain()
            return // Sair do onCreate para não mostrar o layout de login
        }
        // --- FIM DA VERIFICAÇÃO ---

        // Se NÃO temos token, mostramos o ecrã de login
        Log.d("LoginActivity", "Nenhum token encontrado. A mostrar ecrã de login.")
        setContentView(R.layout.activity_login)

        // Inicializar as views (só chegamos aqui se não houver token)
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        btnLogin = findViewById(R.id.btn_login)
        tvErrorMessage = findViewById(R.id.tv_error_message)
        progressBar = findViewById(R.id.progress_bar)

        btnLogin?.setOnClickListener {
            performLogin()
        }

        etEmail?.setText("admin@lojasocial.pt")
        etPassword?.setText("password123")
    }

    private fun performLogin() {
        val email = etEmail?.text.toString().trim()
        val password = etPassword?.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            tvErrorMessage?.text = "Por favor, preencha todos os campos."
            tvErrorMessage?.visibility = View.VISIBLE
            return
        }

        tvErrorMessage?.visibility = View.GONE
        progressBar?.visibility = View.VISIBLE
        btnLogin?.isEnabled = false

        lifecycleScope.launch {
            try {
                val request = LoginRequest(email, password)
                val response = withContext(Dispatchers.IO) {
                    apiService.login(request)
                }

                if (response.success && response.token != null) {
                    // SUCESSO! Guardar o token no "cofre"
                    sessionManager.saveAuthToken(response.token)
                    Log.d("LoginActivity", "Login bem-sucedido! Token guardado.")

                    // Redirecionar para a MainActivity
                    redirectToMain()

                } else {
                    // Login falhou
                    tvErrorMessage?.text = response.message ?: "Email ou password inválidos."
                    tvErrorMessage?.visibility = View.VISIBLE
                    Log.d("LoginActivity", "Login falhou: ${response.message}")
                }

            } catch (e: Exception) {
                Log.e("LoginActivity", "Erro durante o login", e)
                tvErrorMessage?.text = "Erro: ${e.message}"
                tvErrorMessage?.visibility = View.VISIBLE
            } finally {
                progressBar?.visibility = View.GONE
                btnLogin?.isEnabled = true
            }
        }
    }

    /**
     * Função helper para navegar para a MainActivity e fechar a LoginActivity
     */
    private fun redirectToMain() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
        finish() // Fechar a LoginActivity para que o utilizador não possa voltar
    }
}