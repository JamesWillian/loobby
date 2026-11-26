package app.loobby.feature.gameplays.presentation.lobby

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.loobby.core.ui.components.EmptyState

/**
 * O Lobby sem gameplays ativas: o que dá para fazer a partir daqui. Sem nenhuma, o
 * botão flutuante não aparece: a gameplay nova começa pelo botão daqui.
 */
@Composable
fun LobbyEmptyState(
    isAnonymous: Boolean,
    onNewGameplay: () -> Unit,
    onJoinInvite: () -> Unit,
    onCreateGroup: () -> Unit,
    onLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EmptyState(
        icon = Icons.Outlined.SportsEsports,
        title = "Nenhuma gameplay ativa",
        message = if (isAnonymous) {
            "Entre com um convite para ver as gameplays dos amigos. Para criar, faça sua conta."
        } else {
            "Crie uma gameplay, chame os amigos ou entre com um convite."
        },
        modifier = modifier,
    ) {
        if (isAnonymous) {
            Button(onClick = onJoinInvite, modifier = Modifier.fillMaxWidth()) { Text("Entrar com convite") }
            FilledTonalButton(onClick = onLogin, modifier = Modifier.fillMaxWidth()) { Text("Entrar ou criar conta") }
        } else {
            Button(onClick = onNewGameplay, modifier = Modifier.fillMaxWidth()) { Text("Nova gameplay") }
            FilledTonalButton(onClick = onJoinInvite, modifier = Modifier.fillMaxWidth()) { Text("Entrar com convite") }
            OutlinedButton(onClick = onCreateGroup, modifier = Modifier.fillMaxWidth()) { Text("Criar grupo") }
        }
    }
}
