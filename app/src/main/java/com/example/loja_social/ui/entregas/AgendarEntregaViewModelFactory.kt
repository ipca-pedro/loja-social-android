package com.example.loja_social.ui.entregas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.loja_social.repository.AgendarEntregaRepository

class AgendarEntregaViewModelFactory(
    private val repository: AgendarEntregaRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AgendarEntregaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AgendarEntregaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}