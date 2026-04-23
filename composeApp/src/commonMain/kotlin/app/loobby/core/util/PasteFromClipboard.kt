package app.loobby.core.util

import androidx.compose.runtime.Composable

/**
 * Lê o conteúdo textual atual da área de transferência do sistema.
 * Retorna null se o clipboard estiver vazio ou não contiver texto.
 *
 * Segue o mesmo padrão expect/actual de [rememberCopyToClipboard].
 */
@Composable
expect fun rememberPasteFromClipboard(): () -> String?
