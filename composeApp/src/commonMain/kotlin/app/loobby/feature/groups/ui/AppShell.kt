package app.loobby.feature.groups.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import app.loobby.core.lifecycle.OnResumeEffect
import app.loobby.core.navigation.*
import app.loobby.feature.auth.presentation.AuthBottomSheet
import app.loobby.feature.auth.presentation.AuthViewModel
import app.loobby.feature.auth.presentation.ProfileBottomSheet
import app.loobby.feature.events.presentation.CreateEventSheet
import app.loobby.feature.groups.domain.model.FeedType
import app.loobby.feature.groups.presentation.FeedViewModel
import app.loobby.feature.groups.presentation.GroupsViewModel
import app.loobby.feature.auth.presentation.AnonNicknameSheet
import app.loobby.feature.auth.presentation.EmailVerificationBanner
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun AppShell(
    vm: GroupsViewModel = koinInject(),
    feedVm: FeedViewModel = koinInject(),
    authVm: AuthViewModel = koinInject()
) {
    val state by vm.uiState.collectAsState()
    val feedState by feedVm.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // ── Rota inicial baseada no último feed item selecionado ────────
    val initialRoute = remember {
        val lastId = feedVm.getLastSelectedFeedId()
        val lastType = feedVm.getLastSelectedFeedType()
        when {
            lastId != null && lastType == "EVENT" -> AppRoute.EventDetail(eventId = lastId, eventName = "")
            lastId != null -> AppRoute.Group(groupId = lastId, groupName = "")
            else -> AppRoute.Welcome
        }
    }
    val appNavigator = rememberAppNavigator(initialRoute)

    // ── Sheet visibility state ──────────────────────────────────────
    var showActionSheet by remember { mutableStateOf(false) }
    var showCreateGroupSheet by remember { mutableStateOf(false) }
    var showJoinByInviteSheet by remember { mutableStateOf(false) }
    var showInstantEventSheet by remember { mutableStateOf(false) }

    // ── Auth / Profile bottom sheets ────────────────────────────────
    var showAuthSheet by remember { mutableStateOf(false) }
    var showProfileSheet by remember { mutableStateOf(false) }

    // sheet de apelido para usuário anônimo sem nickname personalizado
    var showAnonNicknameSheet by remember { mutableStateOf(false) }

    // nome de boas-vindas para passar ao AuthBottomSheet (anônimo com nickname personalizado)
    var authWelcomeName by remember { mutableStateOf<String?>(null) }

    // ── Auth state ──────────────────────────────────────────────────
    val authState by authVm.uiState.collectAsState()

    OnResumeEffect {
       if (authState.needsEmailVerification) {
           authVm.checkEmailVerification()
       }
   }

    val isOnWelcome = appNavigator.current is AppRoute.Welcome

    // ── Helper: bloqueia ação se não tem full access ────────────────
    fun requireFullAccess(action: () -> Unit) {
        when {
            authState.hasFullAccess -> action()
            authState.isAnonymous -> showAuthSheet = true
            authState.needsEmailVerification -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Verifique seu email para desbloquear esta ação.")
                }
            }
        }
    }

    // ── Action sheet ────────────────────────────────────────────────
    if (showActionSheet) {
        ActionSheet(
            onDismiss = { showActionSheet = false },
            onOptionSelected = { option ->
                showActionSheet = false
                when (option) {
                    ActionSheetOption.CREATE_GROUP -> {
                        requireFullAccess { showCreateGroupSheet = true }
                    }
                    ActionSheetOption.JOIN_BY_INVITE -> {
                        // Liberado para todos
                        showJoinByInviteSheet = true
                    }
                    ActionSheetOption.INSTANT_EVENT -> {
                        requireFullAccess { showInstantEventSheet = true }
                    }
                }
            }
        )
    }

    // ── Create group sheet ──────────────────────────────────────────
    if (showCreateGroupSheet) {
        CreateGroupSheet(
            isLoading = state.isCreatingGroup,
            onDismiss = { showCreateGroupSheet = false },
            onCreateGroup = { name ->
                vm.createNewGroup(name) { groupId, groupName ->
                    showCreateGroupSheet = false
                    appNavigator.navigateRoot(AppRoute.Group(groupId, groupName))
                }
            }
        )
    }

    // ── Join by invite sheet ────────────────────────────────────────
    if (showJoinByInviteSheet) {
        JoinByInviteSheet(
            isLoading = state.isSearchingInvite || state.isJoiningByInvite,
            invitePreview = state.invitePreview,
            errorMessage = state.inviteError,
            onDismiss = { showJoinByInviteSheet = false },
            onSearchInvite = { code -> vm.searchInviteCode(code) },
            onConfirmJoin = {
                vm.confirmJoinByInvite { groupId, groupName ->
                    showJoinByInviteSheet = false
                    appNavigator.navigateRoot(AppRoute.Group(groupId, groupName))
                }
            },
            onClearPreview = { vm.clearInvitePreview() }
        )
    }

    // ── Instant event sheet ─────────────────────────────────────────
    if (showInstantEventSheet) {
        CreateEventSheet(
            groupId = null,
            onDismiss = { showInstantEventSheet = false },
            onEventCreated = {
                showInstantEventSheet = false
                // Refresh feed para incluir o novo evento instantâneo
                feedVm.refreshFeed()
            }
        )
    }

    // ── Auth bottom sheet (login/register) ──────────────────────────
    if (showAuthSheet) {
        AuthBottomSheet(
            welcomeName = authWelcomeName,
            onDismiss = {
                showAuthSheet = false
                authWelcomeName = null
                feedVm.refreshFeed()
            }
        )
    }

    // ── Profile bottom sheet ────────────────────────────────────────
    if (showProfileSheet) {
        ProfileBottomSheet(
            onDismiss = {
                showProfileSheet = false
            }
        )
    }

    // Sheet de apelido para anônimo sem nickname personalizado
    if (showAnonNicknameSheet) {
        AnonNicknameSheet(
            onDismiss = {
                showAnonNicknameSheet = false
            },
            onOpenAuth = {
                showAuthSheet = true
            }
        )
    }

    // ── Auto-navigate quando selectedFeedId/Type muda ───────────────
    // IMPORTANTE: usamos SÓ dados de [feedState] aqui. Não podemos olhar para
    // [state.selectedGroup] (GroupsVM) porque ele é atualizado de forma
    // assíncrona — durante a corrida, o effect acabaria "desfazendo" o click
    // do usuário ao renavegar com o grupo antigo.
    LaunchedEffect(feedState.selectedFeedId, feedState.selectedFeedType, feedState.feed.size, feedState.isLoading) {
        if (!feedState.isLoading) {
            val current = appNavigator.current
            val feedId = feedState.selectedFeedId
            val feedType = feedState.selectedFeedType

            when {
                // Item selecionado é um EVENT → navega para EventDetail como root
                feedId != null && feedType == FeedType.EVENT -> {
                    val feedItem = feedState.feed.find { it.id == feedId }
                    val name = feedItem?.name ?: ""
                    val needsNav = when (current) {
                        is AppRoute.EventDetail ->
                            current.eventId != feedId ||
                                (current.eventName.isBlank() && name.isNotBlank())
                        else -> true
                    }
                    if (needsNav) {
                        appNavigator.navigateRoot(AppRoute.EventDetail(feedId, name))
                    }
                }
                // Item selecionado é um GROUP → navega para Group como root
                feedId != null && feedType == FeedType.GROUP -> {
                    val feedItem = feedState.feed.find { it.id == feedId }
                    val name = feedItem?.name ?: ""
                    val needsNav = when (current) {
                        is AppRoute.Group ->
                            current.groupId != feedId ||
                                (current.groupName.isBlank() && name.isNotBlank())
                        else -> true
                    }
                    if (needsNav) {
                        appNavigator.navigateRoot(AppRoute.Group(feedId, name))
                    }
                }
                // Nenhum item e feed vazio → Welcome
                feedId == null && feedState.feed.isEmpty() && current !is AppRoute.Welcome -> {
                    appNavigator.navigateRoot(AppRoute.Welcome)
                }
            }
        }
    }

    // ── Main content ────────────────────────────────────────────────
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }  // ← NOVO
    ) { innerPadding ->
        Row(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

//            if (!isOnWelcome) {
                GroupSidebar(
                    isLoading = feedState.isLoading,
                    feed = feedState.feed,
                    selectedFeedId = feedState.selectedFeedId,
                    userAvatarUrl = authState.profile?.avatarUrl,
                    onProfileClick = {
                        when {
                            !authState.isAnonymous -> {
                                showProfileSheet = true
                            }
                            isGenericNickname(authState.profile?.displayname) -> {
                                showAnonNicknameSheet = true
                            }
                            else -> {
                                authWelcomeName = authState.profile?.displayname
                                showAuthSheet = true
                            }
                        }
                    },
                    onFeedItemSelected = { id, type ->
                        feedVm.selectFeedItem(id, type)
                        // Navegação imediata para feedback rápido
                        when (type) {
                            FeedType.GROUP -> {
                                val feedItem = feedState.feed.find { it.id == id }
                                appNavigator.navigateRoot(
                                    AppRoute.Group(
                                        groupId = id,
                                        groupName = feedItem?.name ?: ""
                                    )
                                )
                            }
                            FeedType.EVENT -> {
                                val feedItem = feedState.feed.find { it.id == id }
                                appNavigator.navigateRoot(
                                    AppRoute.EventDetail(
                                        eventId = id,
                                        eventName = feedItem?.name ?: ""
                                    )
                                )
                            }
                        }
                    },
                    onCreateOrJoinClick = {
                        showActionSheet = true
                    }
                )
//            }

            // Column para empilhar banner + conteúdo
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 16.dp))
                ) {
                    AppContent(
                        appNavigator = appNavigator,
                        onCreateGroup = { requireFullAccess { showCreateGroupSheet = true } },  // ← ALTERADO
                        onJoinGroup = { showJoinByInviteSheet = true },
                        onInstantEvent = { requireFullAccess { showInstantEventSheet = true } },  // ← ALTERADO
                        onLogin = { showAuthSheet = true },
                    )
                }

                // ── Email verification banner ────────────────────
                EmailVerificationBanner(
                    visible = authState.needsEmailVerification,
                    email = authState.profile?.email,
                    isResending = authState.isResendingVerification,
                    cooldownSeconds = authState.resendCooldownSeconds,
                    message = authState.verificationMessage,
                    onResendClick = { authVm.resendVerificationEmail() },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

private fun isGenericNickname(displayname: String?): Boolean {
    if (displayname == null) return true
    return displayname.startsWith("user_")
}