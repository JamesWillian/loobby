package app.loobby.feature.gameplays.presentation.invite

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import app.loobby.core.ui.collectAsImmediateState
import app.loobby.core.analytics.AppScreen
import app.loobby.core.analytics.TrackScreen
import app.loobby.core.navigation.ScopedViewModelStore
import app.loobby.core.network.LocalIsOnline
import app.loobby.core.ui.components.AppSheet
import app.loobby.core.ui.components.InlineError
import app.loobby.core.ui.components.LoadingButton
import app.loobby.feature.gameplays.presentation.invite.components.GameplayInvitePreviewCard
import app.loobby.feature.gameplays.presentation.invite.components.GroupInvitePreviewCard
import app.loobby.feature.gameplays.presentation.invite.components.InviteCodeField
import org.koin.compose.viewmodel.koinViewModel

/** Entrar com o convite de um grupo ou de uma gameplay única. */
@Composable
fun JoinInviteSheet(onDismiss: () -> Unit, onJoined: (InviteJoined) -> Unit) {
    ScopedViewModelStore {
        JoinInviteSheetContent(onDismiss = onDismiss, onJoined = onJoined)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JoinInviteSheetContent(
    onDismiss: () -> Unit,
    onJoined: (InviteJoined) -> Unit,
    vm: JoinInviteViewModel = koinViewModel(),
) {
    val state by vm.state.collectAsImmediateState()
    val isOnline = LocalIsOnline.current
    TrackScreen(AppScreen.JOIN_BY_INVITE)
    LaunchedEffect(vm) { vm.joined.collect(onJoined) }

    AppSheet(onDismiss = onDismiss, title = "Entrar com convite") {
        Text(
            text = "Cole ou digite o código de um grupo ou de uma gameplay.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        InviteCodeField(code = state.code, enabled = !state.isBusy, onCodeChange = vm::onCodeChange)
        InlineError(message = state.errorMessage)

        when (val found = state.found) {
            null -> LoadingButton(
                text = "Buscar",
                onClick = vm::search,
                isLoading = state.isSearching,
                enabled = isOnline && state.code.isNotBlank(),
            )
            is InviteFound.Group -> {
                GroupInvitePreviewCard(group = found.group)
                LoadingButton(text = "Entrar no grupo", onClick = vm::join, isLoading = state.isJoining, enabled = isOnline)
            }
            is InviteFound.Gameplay -> {
                GameplayInvitePreviewCard(preview = found.preview)
                val label = if (found.preview.joined) "Abrir" else "Entrar na gameplay"
                LoadingButton(
                    text = label,
                    onClick = vm::join,
                    isLoading = state.isJoining,
                    enabled = isOnline && (found.preview.joined || !found.preview.isEnded),
                )
            }
        }
    }
}
