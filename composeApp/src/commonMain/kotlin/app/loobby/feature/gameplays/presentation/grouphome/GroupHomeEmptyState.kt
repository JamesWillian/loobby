package app.loobby.feature.gameplays.presentation.grouphome

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.loobby.core.ui.components.EmptyState

/**
 * O grupo sem gameplays ativas. Sem nenhuma, o botão flutuante não aparece: a
 * gameplay nova começa pelo botão daqui. O anônimo não cria, então não vê o botão.
 */
@Composable
fun GroupHomeEmptyState(
    canCreate: Boolean,
    isOnline: Boolean,
    onNewGameplay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EmptyState(
        icon = Icons.Outlined.SportsEsports,
        title = "Nenhuma gameplay ativa",
        message = "Marque uma gameplay e chame o grupo para jogar.",
        modifier = modifier,
    ) {
        if (canCreate) {
            Button(onClick = onNewGameplay, enabled = isOnline, modifier = Modifier.fillMaxWidth()) {
                Text("Nova gameplay")
            }
        }
    }
}
