package app.loobby.feature.gameplays.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import app.loobby.core.ui.collectAsImmediateState
import app.loobby.core.analytics.AppScreen
import app.loobby.core.analytics.GamePickSource
import app.loobby.core.analytics.TrackScreen
import app.loobby.core.network.LocalIsOnline
import app.loobby.core.ui.LocalAppSnackbar
import app.loobby.core.ui.components.BackTopBar
import app.loobby.core.ui.components.EmptyState
import app.loobby.core.ui.components.InviteCodeCard
import app.loobby.core.util.rememberCopyToClipboard
import app.loobby.feature.games.presentation.GamePickerSheet
import app.loobby.feature.gameplays.domain.model.Gameplay
import app.loobby.feature.gameplays.domain.model.GameplayMode
import app.loobby.feature.gameplays.presentation.detail.components.AnonymousNotice
import app.loobby.feature.gameplays.presentation.detail.components.GameSection
import app.loobby.feature.gameplays.presentation.detail.components.GameplayActionsMenu
import app.loobby.feature.gameplays.presentation.detail.components.GameplayHero
import app.loobby.feature.gameplays.presentation.detail.components.GameplayStatusNotice
import app.loobby.feature.gameplays.presentation.detail.components.ParticipationSection
import app.loobby.feature.gameplays.presentation.detail.components.VotingSection
import app.loobby.feature.gameplays.presentation.form.GameplayFormMode
import app.loobby.feature.gameplays.presentation.form.GameplayFormSheet
import app.loobby.feature.gameplays.presentation.model.toGameChoice
import app.loobby.feature.gameplays.presentation.model.typedGameChoice
import app.loobby.feature.library.domain.model.LibrarySource
import app.loobby.feature.library.presentation.picks.LibraryQuickPicks
import app.loobby.theme.Spacing
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * A gameplay aberta. Todos os botões vêm das permissões que a API devolve para quem
 * lê; o anônimo só vê. [onLeft] e [onDeleted] fecham a tela: depois de sair ou de
 * excluir, não há mais o que ver aqui.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GameplayDetailScreen(
    gameplayId: String,
    isAnonymous: Boolean,
    currentUserId: String?,
    onBack: () -> Unit,
    onLeft: () -> Unit,
    onDeleted: () -> Unit,
    onLogin: () -> Unit,
    vm: GameplayDetailViewModel = koinViewModel(key = "gameplay:$gameplayId") { parametersOf(gameplayId) },
) {
    val state by vm.state.collectAsImmediateState()
    val snackbar = LocalAppSnackbar.current
    val isOnline = LocalIsOnline.current
    val scope = rememberCoroutineScope()
    val copy = rememberCopyToClipboard()

    var dialog by remember { mutableStateOf<DetailDialog?>(null) }
    var isAddingGame by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.load() }
    LaunchedEffect(vm) {
        vm.events.collect { event ->
            when (event) {
                is GameplayDetailEvent.Message -> snackbar.showSnackbar(event.text)
                GameplayDetailEvent.Left -> onLeft()
                GameplayDetailEvent.Deleted -> onDeleted()
            }
        }
    }

    GameplayDetailDialogHost(dialog = dialog, vm = vm, onDismiss = { dialog = null })

    val gameplay = state.gameplay
    if (isAddingGame && gameplay != null) {
        TrackScreen(AppScreen.ADD_VOTING_GAME)
        GamePickerSheet(
            title = "Adicionar jogo",
            onDismiss = { isAddingGame = false },
            onPickCatalogGame = { isAddingGame = false; vm.addGame(it.toGameChoice(), GamePickSource.CATALOG) },
            onPickTypedName = { isAddingGame = false; vm.addGame(typedGameChoice(it), GamePickSource.MANUAL) },
            emptyQueryContent = {
                LibraryQuickPicks(
                    source = gameplay.groupId?.let { LibrarySource.Group(it) } ?: LibrarySource.Mine,
                    exclude = gameplay.voting?.games.orEmpty().mapNotNull { it.catalogId }.toSet(),
                    onPick = { choice, source -> isAddingGame = false; vm.addGame(choice, source) },
                )
            },
        )
    }
    if (isEditing && gameplay != null) {
        GameplayFormSheet(
            mode = GameplayFormMode.Edit(gameplay),
            onDismiss = { isEditing = false },
            onSaved = { saved -> isEditing = false; vm.onEdited(saved) },
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        BackTopBar(
            title = gameplay?.name.orEmpty(),
            onBack = onBack,
            actions = {
                if (gameplay != null) {
                    GameplayActionsMenu(
                        gameplay = gameplay,
                        enabled = isOnline && !state.isBusy,
                        onEdit = { isEditing = true },
                        onEnd = { dialog = DetailDialog.End },
                        onDelete = { dialog = DetailDialog.Delete(isEnded = gameplay.isEnded) },
                        onLeave = { dialog = DetailDialog.Leave },
                    )
                }
            },
        )
        if (state.isBusy || (state.isLoading && gameplay == null)) {
            LinearWavyProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        when {
            gameplay != null -> GameplayDetailContent(
                gameplay = gameplay,
                state = state,
                isAnonymous = isAnonymous,
                isOnline = isOnline,
                currentUserId = currentUserId,
                vm = vm,
                onLogin = onLogin,
                onCopyInvite = { code ->
                    copy(code)
                    scope.launch { snackbar.showSnackbar("Código copiado") }
                },
                onAddGame = { isAddingGame = true },
                onDialog = { dialog = it },
            )
            state.loadError != null -> EmptyState(
                icon = Icons.Outlined.ErrorOutline,
                title = "Não deu para abrir a gameplay",
                message = state.loadError.orEmpty(),
            ) {
                Button(onClick = vm::load, modifier = Modifier.fillMaxWidth()) { Text("Tentar de novo") }
            }
        }
    }
}

@Composable
private fun GameplayDetailContent(
    gameplay: Gameplay,
    state: GameplayDetailUiState,
    isAnonymous: Boolean,
    isOnline: Boolean,
    currentUserId: String?,
    vm: GameplayDetailViewModel,
    onLogin: () -> Unit,
    onCopyInvite: (String) -> Unit,
    onAddGame: () -> Unit,
    onDialog: (DetailDialog) -> Unit,
) {
    val isBusy = state.isBusy || !isOnline
    LazyColumn(
        contentPadding = PaddingValues(horizontal = Spacing.screen, vertical = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.xl),
        modifier = Modifier.fillMaxSize(),
    ) {
        item(key = "hero") { GameplayHero(gameplay) }
        item(key = "status") { GameplayStatusNotice(gameplay) }
        if (isAnonymous) item(key = "anonymous") { AnonymousNotice(onLogin = onLogin) }
        item(key = "participation") {
            ParticipationSection(gameplay = gameplay, isBusy = isBusy, onRespond = vm::respond)
        }
        gameplay.inviteCode?.let { code ->
            item(key = "invite") {
                InviteCodeCard(
                    code = code,
                    hint = "Mande para quem você quer chamar.",
                    onCopy = { onCopyInvite(code) },
                )
            }
        }
        item(key = "game") {
            val voting = gameplay.voting
            when {
                gameplay.mode == GameplayMode.VOTING && voting != null -> VotingSection(
                    voting = voting,
                    permissions = gameplay.permissions,
                    goingCount = gameplay.counts.going,
                    isEnded = gameplay.isEnded,
                    isBusy = isBusy,
                    onVote = vm::vote,
                    onRemoveGame = { onDialog(DetailDialog.RemoveGame(it)) },
                    onAddGame = onAddGame,
                    onTiebreak = vm::openTiebreak,
                )
                else -> GameSection(
                    game = gameplay.game,
                    owners = gameplay.gameOwners,
                    goingCount = gameplay.counts.going,
                )
            }
        }
        commentsSection(
            comments = state.comments,
            canComment = gameplay.permissions.comment,
            isOnline = isOnline,
            currentUserId = currentUserId,
            onDraftChange = vm::onDraftChange,
            onSend = vm::postComment,
            onLoadMore = vm::loadMoreComments,
            onDelete = { onDialog(DetailDialog.DeleteComment(it.id)) },
            onReport = { onDialog(DetailDialog.ReportComment(it.id)) },
        )
    }
}
