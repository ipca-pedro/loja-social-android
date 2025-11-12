package com.example.loja_social.ui.common

import android.view.View
import androidx.core.content.ContextCompat
import com.example.loja_social.R
import com.google.android.material.snackbar.Snackbar

/**
 * Helper object para feedback visual consistente na app.
 */
object UiHelper {

    /**
     * Mostra uma mensagem de sucesso usando Snackbar.
     */
    fun showSuccessMessage(view: View, message: String, duration: Int = Snackbar.LENGTH_SHORT) {
        Snackbar.make(view, message, duration)
            .setBackgroundTint(ContextCompat.getColor(view.context, R.color.success))
            .setTextColor(ContextCompat.getColor(view.context, android.R.color.white))
            .show()
    }

    /**
     * Mostra uma mensagem de erro usando Snackbar.
     */
    fun showErrorMessage(view: View, message: String, duration: Int = Snackbar.LENGTH_LONG) {
        Snackbar.make(view, message, duration)
            .setBackgroundTint(ContextCompat.getColor(view.context, android.R.color.holo_red_dark))
            .setTextColor(ContextCompat.getColor(view.context, android.R.color.white))
            .show()
    }

    /**
     * Mostra uma mensagem de informação usando Snackbar.
     */
    fun showInfoMessage(view: View, message: String, duration: Int = Snackbar.LENGTH_SHORT) {
        Snackbar.make(view, message, duration)
            .setBackgroundTint(ContextCompat.getColor(view.context, R.color.colorPrimary))
            .setTextColor(ContextCompat.getColor(view.context, android.R.color.white))
            .show()
    }

    /**
     * Mostra uma mensagem de aviso usando Snackbar.
     */
    fun showWarningMessage(view: View, message: String, duration: Int = Snackbar.LENGTH_SHORT) {
        Snackbar.make(view, message, duration)
            .setBackgroundTint(ContextCompat.getColor(view.context, android.R.color.holo_orange_dark))
            .setTextColor(ContextCompat.getColor(view.context, android.R.color.white))
            .show()
    }
}

