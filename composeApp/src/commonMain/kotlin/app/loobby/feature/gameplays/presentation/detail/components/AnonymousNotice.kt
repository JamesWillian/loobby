package app.loobby.feature.gameplays.presentation.detail.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Para a conta anônima, no lugar dos botões: o que ela pode fazer e como fazer mais. */
@Composable
fun AnonymousNotice(onLogin: () -> Unit, modifier: Modifier = Modifier) {
    NoticeCard(
        icon = Icons.Outlined.VisibilityOff,
        text = "Conta anônima só vê. Crie uma conta para responder, votar e comentar.",
        modifier = modifier,
    ) {
        TextButton(onClick = onLogin) { Text("Entrar ou criar conta") }
    }
}
