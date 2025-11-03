package com.example.loja_social

import android.app.Application
import com.example.loja_social.api.RetrofitInstance

/**
 * Esta classe é o ponto de entrada da aplicação,
 * antes de qualquer Activity ser criada.
 * É o sítio perfeito para inicializar coisas globais, como o Retrofit.
 */
class LojaSocialApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Inicializar a nossa instância do Retrofit
        // logo que a app arranca
        RetrofitInstance.initialize(this)
    }
}