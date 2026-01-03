package com.example.loja_social.ui.entregas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.loja_social.repository.EntregaRepository

class EntregaDetailViewModelFactory(private val entregaId: String, private val repository: EntregaRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EntregaDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EntregaDetailViewModel(entregaId, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}