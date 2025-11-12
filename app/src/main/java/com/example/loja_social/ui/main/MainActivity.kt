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

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // 1. Ligar o BottomNavigationView ao NavController
        binding.bottomNavView.setupWithNavController(navController)

        // 2. Implementar o Listener para o Log Out
        binding.bottomNavView.setOnItemSelectedListener { item ->
            if (item.itemId == R.id.nav_logout) {
                performLogout()
                true
            } else {
                // Deixa o NavController tratar da navegação normal
                navController.navigate(item.itemId)
                true
            }
        }
    }

    private fun performLogout() {
        // 1. Apaga o token JWT
        SessionManager(applicationContext).clearAuthToken()

        // 2. Navega para a Activity de Login
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Limpa a pilha
        startActivity(intent)
        finish()
    }
}