package com.example.loja_social.ui.beneficiarios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.loja_social.repository.BeneficiarioRepository

/**
 * Factory que permite injetar o Repository e o ID do beneficiário no ViewModel.
 */
class BeneficiarioDetailViewModelFactory(
    private val repository: BeneficiarioRepository,
    private val beneficiarioId: String?
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BeneficiarioDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BeneficiarioDetailViewModel(repository, beneficiarioId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}