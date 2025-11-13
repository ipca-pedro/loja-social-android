package com.example.loja_social.ui.entregas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.loja_social.repository.EntregaRepository

/**
 * Factory para criar instâncias de EntregasViewModel.
 * Permite injetar dependências (Repository) no ViewModel.
 */
class EntregasViewModelFactory(
    private val repository: EntregaRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EntregasViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EntregasViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}