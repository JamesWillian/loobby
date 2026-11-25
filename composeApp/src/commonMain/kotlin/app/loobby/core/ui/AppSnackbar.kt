package app.loobby.core.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * O snackbar do app, dono da barra de baixo. As telas mostram mensagens nele em vez
 * de terem cada uma o seu: assim o aviso aparece sempre no mesmo lugar, acima da
 * barra, e sobrevive à troca de tela.
 */
val LocalAppSnackbar = staticCompositionLocalOf<SnackbarHostState> {
    error("LocalAppSnackbar não foi fornecido: o AppShell provê o snackbar do app.")
}
