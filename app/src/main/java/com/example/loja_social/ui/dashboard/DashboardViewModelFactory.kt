package com.example.loja_social.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.loja_social.repository.DashboardRepository

/**
 * Factory para criar instâncias de DashboardViewModel.
 * Permite injetar dependências (Repository) no ViewModel.
 */
class DashboardViewModelFactory(
    private val repository: DashboardRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}