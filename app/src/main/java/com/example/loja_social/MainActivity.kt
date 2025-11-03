package com.example.loja_social

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.loja_social.api.RetrofitInstance // Importar a nossa instância
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    // Usamos a instância da API que já foi inicializada
    private val apiService = RetrofitInstance.api
    private lateinit var tvResultados: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvResultados = findViewById(R.id.tv_resultados)

        // Vamos tentar buscar os beneficiários (rota protegida)
        fetchProtectedData()
    }

    private fun fetchProtectedData() {
        tvResultados.text = "A carregar dados protegidos (Beneficiários)..."

        lifecycleScope.launch {
            try {
                // MUDAMOS para a thread de IO para a chamada de rede
                val response = withContext(Dispatchers.IO) {
                    apiService.getBeneficiarios() // A CHAMADA PROTEGIDA!
                }

                // VOLTAMOS à thread Principal para atualizar a UI
                if (response.success) {
                    val nomes = response.data.joinToString("\n") { "• ${it.nomeCompleto}" }
                    tvResultados.text = "Beneficiários Encontrados (Login OK!):\n\n$nomes"
                } else {
                    tvResultados.text = "A API negou o acesso (Login Falhou?)"
                }

            } catch (e: Exception) {
                // Se falhar (ex: 401 Unauthorized, 403 Forbidden)
                Log.e("MainActivity", "Erro ao buscar beneficiários", e)
                tvResultados.text = "Falha ao carregar dados protegidos: ${e.message}"
            }
        }
    }
}