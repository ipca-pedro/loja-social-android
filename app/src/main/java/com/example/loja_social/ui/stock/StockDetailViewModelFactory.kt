package com.example.loja_social.ui.stock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.loja_social.repository.StockRepository

class StockDetailViewModelFactory(
    private val repository: StockRepository,
    private val produtoId: Int
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StockDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StockDetailViewModel(repository, produtoId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

