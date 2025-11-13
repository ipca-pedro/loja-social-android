package com.example.loja_social.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.example.loja_social.R
import com.example.loja_social.databinding.ActivityMainBinding
import androidx.navigation.ui.setupWithNavController
import com.example.loja_social.SessionManager // Import necessário
import com.example.loja_social.ui.login.LoginActivity // Import necessário
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Activity principal da aplicação.
 * Gerencia a navegação entre fragments usando Navigation Component e BottomNavigationView.
 * Inclui funcionalidade de logout com confirmação.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configura Navigation Component
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Liga o BottomNavigationView ao NavController para navegação automática
        binding.bottomNavView.setupWithNavController(navController)

        // Implementa listener customizado para tratar logout com confirmação
        binding.bottomNavView.setOnItemSelectedListener { item ->
            if (item.itemId == R.id.nav_logout) {
                // Mostra diálogo de confirmação antes de fazer logout
                showLogoutConfirmation()
                false // Não navega ainda, aguarda confirmação
            } else {
                // Navegação normal para outros itens do menu
                navController.navigate(item.itemId)
                true
            }
        }
    }

    /**
     * Mostra um diálogo de confirmação antes de fazer logout.
     */
    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Confirmar Logout")
            .setMessage("Tem certeza que deseja sair?")
            .setPositiveButton("Sair") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * Executa o logout: remove o token e navega para a tela de login.
     * Limpa a pilha de activities para evitar voltar atrás.
     */
    private fun performLogout() {
        // Remove o token JWT das SharedPreferences
        SessionManager(applicationContext).clearAuthToken()

        // Navega para a Activity de Login e limpa a pilha de activities
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}