package app.loobby.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

// ── Theme ─────────────────────────────────────────────────────────────────────

/**
 * Destaque da marca, que acompanha o tema como as variantes do símbolo 4P: verde-limão
 * sobre o fundo escuro, azul sobre o claro. Marca o item escolhido na barra lateral.
 */
val LocalBrandAccent = staticCompositionLocalOf { FourPlayersColors.Primary }

@Composable
fun FourPlayersTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkScheme else LightScheme
    val brandAccent = if (darkTheme) FourPlayersColors.Volt else FourPlayersColors.Primary

    CompositionLocalProvider(LocalBrandAccent provides brandAccent) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
