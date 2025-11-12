package com.example.loja_social.ui.main // O package será corrigido no Passo 3

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.loja_social.repository.BeneficiarioRepository

// NOME DA CLASSE MUDADO
class BeneficiariosViewModelFactory(
    private val repository: BeneficiarioRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // CONDIÇÃO MUDADA
        if (modelClass.isAssignableFrom(BeneficiariosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // CONSTRUTOR MUDADO
            return BeneficiariosViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}