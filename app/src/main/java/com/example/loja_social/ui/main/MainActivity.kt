package com.example.loja_social.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.example.loja_social.R
import com.example.loja_social.databinding.ActivityMainBinding
import androidx.navigation.ui.setupWithNavController
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        // 1. Encontrar o NavController
        // O NavHostFragment é o <FragmentContainerView> que definiste no XML
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // 2. Ligar o BottomNavigationView ao NavController
        // Esta linha mágica faz tudo:
        binding.bottomNavView.setupWithNavController(navController)


    }
}