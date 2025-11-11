package com.example.loja_social.uilogin

/**
 * Representa os diferentes estados possíveis da UI de Login.
 * É uma "sealed interface" para que o compilador saiba que não podem existir outros estados.
 */
sealed interface LoginUiState {
    /**
     * Estado inicial, a UI está à espera de interação.
     */
    object Idle : LoginUiState

    /**
     * O botão de login foi clicado, estamos a contactar a API.
     */
    object Loading : LoginUiState

    /**
     * O login foi bem-sucedido e o token foi guardado.
     */
    object Success : LoginUiState

    /**
     * O login falhou (ex: credenciais erradas) ou houve um erro de rede.
     * @param message A mensagem de erro a mostrar ao utilizador.
     */
    data class Error(val message: String) : LoginUiState
}