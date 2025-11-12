package com.example.loja_social.ui.stock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.loja_social.repository.StockRepository

class StockListViewModelFactory(
    private val repository: StockRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StockListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StockListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

