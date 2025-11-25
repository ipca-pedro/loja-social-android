
package com.example.loja_social.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val LightColorPalette = lightColors(
    primary = VerdePrimario,
    primaryVariant = VerdeEscuro,
    secondary = VerdeAcento,
    background = VerdeFundo,
    surface = White,
    onPrimary = White,
    onSecondary = Black,
    onBackground = Black,
    onSurface = Black,
)

@Composable
fun LojaSocialTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = LightColorPalette // O seu app parece ser light theme, vamos manter assim por agora

    MaterialTheme(
        colors = colors,
        typography = Typography, // Esta variável será criada a seguir
        shapes = Shapes, // Esta variável será criada a seguir
        content = content
    )
}
