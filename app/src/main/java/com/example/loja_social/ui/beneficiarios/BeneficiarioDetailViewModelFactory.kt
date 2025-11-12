package com.example.loja_social.ui.beneficiarios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.loja_social.api.Beneficiario
import com.example.loja_social.repository.BeneficiarioRepository

/**
 * Factory que permite injetar dependências (Repository, ID e lista de cache) no ViewModel.
 */
class BeneficiarioDetailViewModelFactory(
    private val repository: BeneficiarioRepository,
    private val beneficiariosList: List<Beneficiario>,
    private val beneficiarioId: String?
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BeneficiarioDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BeneficiarioDetailViewModel(repository, beneficiariosList, beneficiarioId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}