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

    // --- NOVO "COFRE" ---
    private lateinit var sessionManager: SessionManager
    // --- FIM DO NOVO "COFRE" ---

    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var tvErrorMessage: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // --- INICIALIZAR O "COFRE" ---
        sessionManager = SessionManager(applicationContext)
        // --- FIM DA INICIALIZAÇÃO ---

        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        btnLogin = findViewById(R.id.btn_login)
        tvErrorMessage = findViewById(R.id.tv_error_message)
        progressBar = findViewById(R.id.progress_bar)

        btnLogin.setOnClickListener {
            performLogin()
        }

        etEmail.setText("admin@lojasocial.pt")
        etPassword.setText("password123")
    }

    private fun performLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            tvErrorMessage.text = "Por favor, preencha todos os campos."
            tvErrorMessage.visibility = View.VISIBLE
            return
        }

        tvErrorMessage.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        btnLogin.isEnabled = false

        lifecycleScope.launch {
            try {
                val request = LoginRequest(email, password)
                val response = withContext(Dispatchers.IO) {
                    apiService.login(request)
                }

                // --- LÓGICA DE LOGIN ATUALIZADA ---
                // Agora verificamos se o 'token' veio
                if (response.success && response.token != null) {

                    // SUCESSO! Guardar o token no "cofre"
                    sessionManager.saveAuthToken(response.token)

                    Log.d("LoginActivity", "Login bem-sucedido! Token guardado.")

                    // Redirecionar para a MainActivity
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()

                } else {
                    // Login falhou (API disse success: false ou não enviou token)
                    tvErrorMessage.text = response.message ?: "Email ou password inválidos."
                    tvErrorMessage.visibility = View.VISIBLE
                    Log.d("LoginActivity", "Login falhou: ${response.message}")
                }
                // --- FIM DA LÓGICA DE LOGIN ---

            } catch (e: Exception) {
                Log.e("LoginActivity", "Erro durante o login", e)
                tvErrorMessage.text = "Erro: ${e.message}"
                tvErrorMessage.visibility = View.VISIBLE
            } finally {
                progressBar.visibility = View.GONE
                btnLogin.isEnabled = true
            }
        }
    }
}