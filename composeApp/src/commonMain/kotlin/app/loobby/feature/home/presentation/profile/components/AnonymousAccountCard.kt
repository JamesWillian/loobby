package app.loobby.feature.home.presentation.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.loobby.core.ui.components.UserAvatar
import app.loobby.feature.auth.data.model.UserMeResponse
import app.loobby.theme.Spacing

/** O topo do Perfil do anônimo: o apelido, o que a conta anônima não faz e o caminho para a conta. */
@Composable
fun AnonymousAccountCard(
    profile: UserMeResponse?,
    onLogin: () -> Unit,
    onChooseNickname: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = Spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        UserAvatar(url = profile?.avatarUrl, size = 96.dp)
        Text(
            text = profile?.displayname?.takeIf { it.isNotBlank() } ?: profile?.username.orEmpty(),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Conta anônima: você vê os grupos e gameplays em que entrou, mas não responde, vota nem comenta.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Column(
            modifier = Modifier.widthIn(max = 320.dp).fillMaxWidth().padding(top = Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Button(onClick = onLogin, modifier = Modifier.fillMaxWidth()) { Text("Entrar ou criar conta") }
            FilledTonalButton(onClick = onChooseNickname, modifier = Modifier.fillMaxWidth()) { Text("Escolher apelido") }
        }
    }
}
