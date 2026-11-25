package app.loobby.feature.home.presentation.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import app.loobby.core.ui.components.ScreenHeader
import app.loobby.core.ui.components.UserAvatar
import app.loobby.feature.auth.data.model.UserMeResponse
import app.loobby.theme.Spacing

/**
 * A aba Perfil, no mínimo: quem está logado e o caminho para o sheet de perfil de
 * hoje (editar, senha, sair, excluir conta). A tela completa, com a biblioteca de
 * jogos, é da Fase 3.
 */
@Composable
fun ProfileTabScreen(
    profile: UserMeResponse?,
    isAnonymous: Boolean,
    onEditProfile: () -> Unit,
    onLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(title = "Perfil")
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xxl, vertical = Spacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            UserAvatar(url = profile?.avatarUrl, size = 96.dp)
            Text(
                text = profile?.displayname?.takeIf { it.isNotBlank() } ?: profile?.username.orEmpty(),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
            )
            if (!isAnonymous && profile != null) {
                Text(
                    text = "@${profile.username}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(
                    text = "Conta anônima: você vê os grupos e gameplays em que entrou, mas não responde, vota nem comenta.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            Column(
                modifier = Modifier.widthIn(max = 320.dp).fillMaxWidth().padding(top = Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                if (isAnonymous) {
                    Button(onClick = onLogin, modifier = Modifier.fillMaxWidth()) { Text("Entrar ou criar conta") }
                    FilledTonalButton(onClick = onEditProfile, modifier = Modifier.fillMaxWidth()) { Text("Escolher apelido") }
                } else {
                    Button(onClick = onEditProfile, modifier = Modifier.fillMaxWidth()) { Text("Editar perfil e conta") }
                }
            }
        }
    }
}
