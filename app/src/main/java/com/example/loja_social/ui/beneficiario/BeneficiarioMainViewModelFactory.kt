package com.example.loja_social.ui.beneficiario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.loja_social.api.ApiService

class BeneficiarioMainViewModelFactory(
    private val apiService: ApiService
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BeneficiarioMainViewModel::class.java)) {
            return BeneficiarioMainViewModel(apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}