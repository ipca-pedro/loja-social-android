package com.example.loja_social.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.loja_social.data.repository.LoginRepository
import com.example.loja_social.data.session.SessionManager // Assumindo que moveste o SessionManager para data.session

/**
 * Fábrica para criar o LoginViewModel.
 * É necessária porque o LoginViewModel tem dependências no construtor
 * (LoginRepository e SessionManager) e o Android não sabe como injetá-las sozinho.
 */
class LoginViewModelFactory(
    private val loginRepository: LoginRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(loginRepository, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}