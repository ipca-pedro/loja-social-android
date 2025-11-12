package com.example.loja_social.ui.common

import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import com.example.loja_social.R
import com.google.android.material.snackbar.Snackbar

/**
 * Helper reutilizável para mostrar mensagens de feedback ao utilizador.
 * Usa Snackbar para mensagens temporárias e cards para mensagens persistentes.
 */
object MessageHelper {

    /**
     * Mostra uma mensagem de sucesso usando Snackbar.
     */
    fun showSuccess(view: View, message: String, duration: Int = Snackbar.LENGTH_SHORT) {
        Snackbar.make(view, message, duration)
            .setBackgroundTint(ContextCompat.getColor(view.context, R.color.success))
            .setTextColor(ContextCompat.getColor(view.context, android.R.color.white))
            .show()
    }

    /**
     * Mostra uma mensagem de erro usando Snackbar.
     */
    fun showError(view: View, message: String, duration: Int = Snackbar.LENGTH_LONG) {
        Snackbar.make(view, message, duration)
            .setBackgroundTint(ContextCompat.getColor(view.context, android.R.color.holo_red_dark))
            .setTextColor(ContextCompat.getColor(view.context, android.R.color.white))
            .show()
    }

    /**
     * Mostra uma mensagem de informação usando Snackbar.
     */
    fun showInfo(view: View, message: String, duration: Int = Snackbar.LENGTH_SHORT) {
        Snackbar.make(view, message, duration)
            .setBackgroundTint(ContextCompat.getColor(view.context, android.R.color.holo_blue_dark))
            .setTextColor(ContextCompat.getColor(view.context, android.R.color.white))
            .show()
    }

    /**
     * Mostra uma mensagem de aviso usando Snackbar.
     */
    fun showWarning(view: View, message: String, duration: Int = Snackbar.LENGTH_LONG) {
        Snackbar.make(view, message, duration)
            .setBackgroundTint(ContextCompat.getColor(view.context, android.R.color.holo_orange_dark))
            .setTextColor(ContextCompat.getColor(view.context, android.R.color.white))
            .show()
    }

    /**
     * Mostra uma mensagem de sucesso com ação.
     */
    fun showSuccessWithAction(
        view: View,
        message: String,
        actionLabel: String,
        action: () -> Unit
    ) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(ContextCompat.getColor(view.context, R.color.success))
            .setTextColor(ContextCompat.getColor(view.context, android.R.color.white))
            .setAction(actionLabel) { action() }
            .setActionTextColor(ContextCompat.getColor(view.context, android.R.color.white))
            .show()
    }
}

