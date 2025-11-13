package com.example.loja_social.ui.stock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.loja_social.repository.StockRepository

/**
 * Factory para criar instâncias de StockViewModel.
 * Permite injetar dependências (Repository) no ViewModel.
 */
class StockViewModelFactory(
    private val repository: StockRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StockViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StockViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}