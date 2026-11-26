package app.loobby.feature.home.presentation.history

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.loobby.core.ui.components.EmptyState

/**
 * O Histórico vazio. O anônimo não marca Vou, então nunca tem histórico: a tela
 * explica e oferece a conta.
 */
@Composable
fun HistoryEmptyState(isAnonymous: Boolean, onLogin: () -> Unit, modifier: Modifier = Modifier) {
    if (isAnonymous) {
        EmptyState(
            icon = Icons.Outlined.History,
            title = "Conta anônima não tem histórico",
            message = "O histórico guarda as gameplays em que você marcou Vou. Com uma conta, você responde, vota e comenta.",
            modifier = modifier,
        ) {
            Button(onClick = onLogin, modifier = Modifier.fillMaxWidth()) { Text("Entrar ou criar conta") }
        }
    } else {
        EmptyState(
            icon = Icons.Outlined.History,
            title = "Nada por aqui ainda",
            message = "As gameplays em que você marcou Vou aparecem aqui depois de encerradas, com o que cada uma jogou.",
            modifier = modifier,
        )
    }
}
