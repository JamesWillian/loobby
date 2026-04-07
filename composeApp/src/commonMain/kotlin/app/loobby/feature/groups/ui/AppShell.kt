package app.loobby.feature.groups.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import app.loobby.core.navigation.*
import app.loobby.feature.auth.presentation.AuthBottomSheet
import app.loobby.feature.auth.presentation.AuthViewModel
import app.loobby.feature.auth.presentation.ProfileBottomSheet
import app.loobby.feature.events.presentation.CreateEventSheet
import app.loobby.feature.groups.domain.model.FeedType
import app.loobby.feature.groups.presentation.GroupsViewModel
import app.loobby.feature.auth.presentation.AnonNicknameSheet
import org.koin.compose.koinInject

@Composable
fun AppShell(
    vm: GroupsViewModel = koinInject(),
    authVm: AuthViewModel = koinInject()
) {
    val state by vm.uiState.collectAsState()

    // ── Rota inicial baseada no último feed item selecionado ────────
    val initialRoute = remember {
        val lastId = vm.getLastSelectedFeedId()
        val lastType = vm.getLastSelectedFeedType()
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

    val isOnWelcome = appNavigator.current is AppRoute.Welcome

    // ── Action sheet ────────────────────────────────────────────────
    if (showActionSheet) {
        ActionSheet(
            onDismiss = { showActionSheet = false },
            onOptionSelected = { option ->
                showActionSheet = false
                when (option) {
                    ActionSheetOption.CREATE_GROUP -> showCreateGroupSheet = true
                    ActionSheetOption.JOIN_BY_INVITE -> showJoinByInviteSheet = true
                    ActionSheetOption.INSTANT_EVENT -> showInstantEventSheet = true
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
                vm.refreshMyFeed()
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
                vm.refreshMyFeed()
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
    LaunchedEffect(state.selectedFeedId, state.selectedFeedType, state.feed.size, state.isLoading) {
        if (!state.isLoading) {
            val current = appNavigator.current
            val feedId = state.selectedFeedId
            val feedType = state.selectedFeedType

            when {
                // Item selecionado é um EVENT → navega para EventDetail como root
                feedId != null && feedType == FeedType.EVENT -> {
                    val feedItem = state.feed.find { it.id == feedId }
                    val name = feedItem?.name ?: ""
                    if (current !is AppRoute.EventDetail || current.eventId != feedId) {
                        appNavigator.navigateRoot(AppRoute.EventDetail(feedId, name))
                    }
                }
                // Item selecionado é um GROUP → navega para Group como root
                feedId != null && feedType == FeedType.GROUP -> {
                    val group = state.selectedGroup
                    if (group != null && (current is AppRoute.Welcome || current is AppRoute.Group || current is AppRoute.EventDetail)) {
                        appNavigator.navigateRoot(AppRoute.Group(group.id, group.name))
                    }
                }
                // Nenhum item e feed vazio → Welcome
                feedId == null && state.feed.isEmpty() && current !is AppRoute.Welcome -> {
                    appNavigator.navigateRoot(AppRoute.Welcome)
                }
            }
        }
    }

    // ── Main content ────────────────────────────────────────────────
    Scaffold { innerPadding ->
        Row(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            if (!isOnWelcome) {
                GroupSidebar(
                    isLoading = state.isLoading,
                    feed = state.feed,                              // ← alterado: feed ao invés de groups
                    selectedFeedId = state.selectedFeedId,          // ← alterado: id genérico
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
                    onFeedItemSelected = { id, type ->              // ← alterado: callback genérico
                        vm.selectFeedItem(id, type)
                        // Navegação imediata para feedback rápido
                        when (type) {
                            FeedType.GROUP -> {
                                val feedItem = state.feed.find { it.id == id }
                                appNavigator.navigateRoot(
                                    AppRoute.Group(
                                        groupId = id,
                                        groupName = feedItem?.name ?: ""
                                    )
                                )
                            }
                            FeedType.EVENT -> {
                                val feedItem = state.feed.find { it.id == id }
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
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 16.dp))
            ) {
                AppContent(
                    appNavigator = appNavigator,
                    onCreateGroup = { showCreateGroupSheet = true },
                    onJoinGroup = { showJoinByInviteSheet = true },
                    onInstantEvent = { showInstantEventSheet = true },
                    onLogin = { showAuthSheet = true },
                )
            }
        }
    }
}

private fun isGenericNickname(displayname: String?): Boolean {
    if (displayname == null) return true
    return displayname.startsWith("user_")
}