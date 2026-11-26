package app.loobby.feature.library.presentation.group

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import app.loobby.core.analytics.AppScreen
import app.loobby.core.ui.LocalAppSnackbar
import app.loobby.core.ui.components.ConfirmDialog
import app.loobby.core.ui.components.InlineError
import app.loobby.feature.library.domain.model.GroupLibraryGame
import app.loobby.feature.library.domain.model.LibraryRules
import app.loobby.feature.library.presentation.components.AddLibraryGameSheet
import app.loobby.feature.library.presentation.components.LibraryGameRow
import app.loobby.feature.library.presentation.components.addedByLine
import app.loobby.feature.library.presentation.mine.LibrarySectionHeader
import app.loobby.theme.Spacing
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * A biblioteca do grupo, nos Detalhes do grupo: os jogos que o grupo joga, que
 * aparecem primeiro na hora de montar a votação. Com [canEdit] (membro que não é
 * anônimo, com rede), a pessoa adiciona e, tocando no jogo, tira.
 */
@Composable
fun GroupLibrarySection(
    groupId: String,
    canEdit: Boolean,
    modifier: Modifier = Modifier,
    vm: GroupLibraryViewModel = koinViewModel(key = "group-library:$groupId") { parametersOf(groupId) },
) {
    val state by vm.state.collectAsState()
    val snackbar = LocalAppSnackbar.current
    var isAdding by remember { mutableStateOf(false) }
    var toRemove by remember { mutableStateOf<GroupLibraryGame?>(null) }

    LaunchedEffect(Unit) { vm.refresh() }
    LaunchedEffect(vm) { vm.messages.collect { snackbar.showSnackbar(it) } }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        LibrarySectionHeader(
            title = "Biblioteca do grupo",
            count = state.games.size,
            canAdd = canEdit && !state.isSaving && state.games.size < LibraryRules.MAX_GROUP,
            onAdd = { isAdding = true },
        )
        Text(
            text = "Os jogos que o grupo joga. Eles aparecem primeiro na hora de escolher o jogo de uma gameplay.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        InlineError(message = state.errorMessage)
        if (!state.isLoading && state.games.isEmpty()) {
            Text(
                text = "Nenhum jogo ainda.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        state.games.forEach { game ->
            LibraryGameRow(
                name = game.name,
                coverUrl = game.coverUrl,
                detail = game.addedByLine(),
                onClick = if (canEdit && !state.isSaving) ({ toRemove = game }) else null,
            )
        }
    }

    if (isAdding) {
        AddLibraryGameSheet(
            title = "Adicionar à biblioteca do grupo",
            screen = AppScreen.ADD_TO_GROUP_LIBRARY,
            onDismiss = { isAdding = false },
            onPick = { game -> isAdding = false; vm.add(game) },
        )
    }
    toRemove?.let { game ->
        ConfirmDialog(
            title = "Tirar da biblioteca do grupo?",
            message = "${game.name} sai da biblioteca do grupo. As gameplays em que ele já está não mudam.",
            confirmLabel = "Tirar",
            destructive = true,
            onConfirm = { toRemove = null; vm.remove(game) },
            onDismiss = { toRemove = null },
        )
    }
}
