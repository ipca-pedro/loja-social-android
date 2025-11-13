package com.example.loja_social.ui.beneficiarios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.loja_social.repository.BeneficiarioRepository

/**
 * Factory para criar instâncias de BeneficiariosViewModel.
 * Permite injetar dependências (Repository) no ViewModel.
 */
class BeneficiariosViewModelFactory(
    private val repository: BeneficiarioRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BeneficiariosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BeneficiariosViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}