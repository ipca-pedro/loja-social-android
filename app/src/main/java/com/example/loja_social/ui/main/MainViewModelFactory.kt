package com.example.loja_social.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.loja_social.repository.BeneficiarioRepository

/**
 * Esta classe é o "Factory" (fábrica) que diz ao Android como criar
 * o nosso MainViewModel, passando-lhe o BeneficiarioRepository
 * de que ele precisa.
 */
class MainViewModelFactory(
    private val repository: BeneficiarioRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Verifica se a classe que o sistema está a pedir é o MainViewModel
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            // Se for, cria um novo e entrega-o
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        // Se for outro ViewModel qualquer, lança um erro
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}