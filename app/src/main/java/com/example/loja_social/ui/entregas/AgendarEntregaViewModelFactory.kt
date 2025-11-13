package com.example.loja_social.ui.entregas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.loja_social.repository.AgendarEntregaRepository
import com.example.loja_social.repository.StockRepository

class AgendarEntregaViewModelFactory(
    private val repository: AgendarEntregaRepository,
    private val stockRepository: StockRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AgendarEntregaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AgendarEntregaViewModel(repository, stockRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}