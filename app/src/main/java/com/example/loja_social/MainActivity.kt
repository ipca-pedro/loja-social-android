package com.example.loja_social // Certifique-se que o nome do pacote está correto

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

    // 1. Obter a nossa instância da API
    private val apiService = RetrofitInstance.api

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 2. Ligar esta classe ao nosso ficheiro XML
        setContentView(R.layout.activity_main)

        // 3. Encontrar o TextView que criámos no layout
        val tvResultados = findViewById<TextView>(R.id.tv_resultados)

        // 4. Lançar uma Coroutine para fazer a chamada de rede
        // lifecycleScope garante que a chamada é cancelada se o ecrã for destruído
        lifecycleScope.launch {
            try {
                // MUDANÇA AQUI: Recebemos o 'response' (o envelope)
                val response = withContext(Dispatchers.IO) {
                    apiService.getCampanhas()
                }

                // Agora, verificamos o 'response'
                // 1. Vemos se a API disse 'success: true'
                // 2. Acedemos à lista com 'response.data'
                if (response.success && response.data.isNotEmpty()) {

                    // Usamos 'response.data' para construir a lista de nomes
                    val nomes = response.data.joinToString(separator = "\n") {
                        "• ${it.nome}"
                    }
                    tvResultados.text = "Campanhas Ativas:\n\n$nomes"

                } else if (response.success && response.data.isEmpty()) {
                    tvResultados.text = "Nenhuma campanha encontrada."
                } else {
                    tvResultados.text = "A API retornou um erro."
                }

            } catch (e: Exception) {
                // O erro original (JSON mal formatado, etc.) ainda pode acontecer
                Log.e("MainActivity", "Erro ao buscar campanhas", e)
                tvResultados.text = "Falha ao carregar dados: ${e.message}"
            }
        }
    }
}