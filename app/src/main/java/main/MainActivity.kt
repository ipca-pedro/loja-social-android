package com.example.loja_social.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.loja_social.R
import com.example.loja_social.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- ESTA É A NOVA LÓGICA ---

        // 1. Encontrar o NavController
        // O NavHostFragment é o <FragmentContainerView> que definiste no XML
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // 2. Ligar o BottomNavigationView ao NavController
        // Esta linha mágica faz tudo:
        // - Deteta cliques no menu (binding.bottomNavView)
        // - Encontra o item de menu clicado (ex: @id/nav_beneficiarios)
        // - Procura no nav_graph.xml por esse mesmo ID
        // - Navega o navController para o Fragment correspondente (ex: BeneficiariosFragment)
        binding.bottomNavView.setupWithNavController(navController)

        // --- FIM DA NOVA LÓGICA ---
    }
}