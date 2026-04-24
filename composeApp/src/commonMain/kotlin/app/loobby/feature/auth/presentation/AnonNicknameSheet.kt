package app.loobby.feature.auth.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.loobby.core.network.LocalIsOnline
import org.koin.compose.koinInject

/**
 * Sheet simplificado para usuários anônimos que ainda não definiram um apelido.
 * Exibe apenas o campo de apelido (displayname) + botão salvar + opção de criar/entrar.
 *
 * Aberto pelo AppShell quando: isAnonymous == true && displayname starts with "user_"
 *
 * @param onDismiss chamado após salvar o apelido ou ao fechar manualmente
 * @param onOpenAuth chamado quando o usuário quer criar conta ou fazer login
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnonNicknameSheet(
    onDismiss: () -> Unit,
    onOpenAuth: () -> Unit,
    vm: ProfileViewModel = koinInject()
) {
    val state by vm.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    // Salvar o apelido anônimo é escrita; desabilita offline.
    val isOnline = LocalIsOnline.current

    // Inicializa o campo com o displayname atual (ou vazio para o usuário digitar)
    LaunchedEffect(state.profile) {
        val current = state.profile?.displayname ?: ""
        // Pré-preenche apenas se já houver algo diferente de user_ (não deve ocorrer aqui, mas por segurança)
        if (!current.startsWith("user_")) {
            vm.onDisplaynameChanged(current)
        } else {
            // Limpa para o usuário digitar um apelido novo
            vm.onDisplaynameChanged("")
        }
    }

    // Fecha o sheet após salvar com sucesso
    LaunchedEffect(state.shouldDismiss) {
        if (state.shouldDismiss) {
            vm.resetDismiss()
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Como quer ser chamado?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Defina um apelido para aparecer nos grupos e eventos",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(28.dp))

            OutlinedTextField(
                value = state.editDisplayname,
                onValueChange = vm::onDisplaynameChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Apelido") },
                placeholder = { Text("Ex: Zé, Maria, Galinha...") },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                isError = state.errorMessage != null
            )

            if (state.errorMessage != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = state.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { saveNickname(vm) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = state.editDisplayname.isNotBlank() && !state.isSaving && isOnline,
                shape = MaterialTheme.shapes.medium
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Salvar apelido", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(Modifier.height(24.dp))

            HorizontalDivider()

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Quer fazer mais?",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    onDismiss()
                    onOpenAuth()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "Criar conta ou Entrar",
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}

/**
 * Salva apenas o displayname — não toca no username.
 * Reutiliza o saveProfile() do ProfileViewModel mas garantindo que só displayname é enviado.
 */
private fun saveNickname(vm: ProfileViewModel) {
    val state = vm.uiState.value
    val displayname = state.editDisplayname.trim()

    if (displayname.isBlank()) return

    // Força username como null para não ser alterado: passamos o mesmo username atual
    // O saveProfile() só envia o que mudou — se editUsername == profile.username, usernameToSend = null
    // Portanto basta garantir que editUsername está correto (igual ao atual)
    val currentUsername = state.profile?.username ?: ""
    if (state.editUsername != currentUsername) {
        vm.onUsernameChanged(currentUsername)
    }

    vm.saveProfile()
}