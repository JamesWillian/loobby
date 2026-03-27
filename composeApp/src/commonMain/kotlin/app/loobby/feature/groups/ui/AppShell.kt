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
import app.loobby.feature.groups.presentation.GroupsViewModel
import app.loobby.feature.auth.presentation.AnonNicknameSheet
import org.koin.compose.koinInject

@Composable
fun AppShell(
    vm: GroupsViewModel = koinInject(),
    authVm: AuthViewModel = koinInject()
) {
    val state by vm.uiState.collectAsState()
    val appNavigator = rememberAppNavigator()

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
                vm.refreshMyGroups()
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
                // Usuário quer criar conta/logar após definir o apelido
                showAuthSheet = true
            }
        )
    }

    // ── Auto-navigate when selectedGroup changes ────────────────────
    LaunchedEffect(state.selectedGroup, state.groups.size, state.isLoading) {
        if (!state.isLoading) {
            val group = state.selectedGroup
            if (group != null) {
                appNavigator.navigateRoot(
                    AppRoute.Group(group.id, group.name)
                )
            } else if (state.groups.isEmpty()) {
                appNavigator.navigateRoot(AppRoute.Welcome)
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
                    groups = state.groups,
                    selectedGroupId = state.selectedGroup?.id,
                    userAvatarUrl = authState.profile?.avatarUrl,
                    onProfileClick = {
                        when {
                            // Usuário registrado → abre perfil normalmente
                            !authState.isAnonymous -> {
                                showProfileSheet = true
                            }

                            // Anônimo sem nickname personalizado → abre sheet de apelido
                            isGenericNickname(authState.profile?.displayname) -> {
                                showAnonNicknameSheet = true
                            }

                            // Anônimo com nickname personalizado → abre login com boas-vindas
                            else -> {
                                authWelcomeName = authState.profile?.displayname
                                showAuthSheet = true
                            }
                        }
                    },
                    onGroupSelected = { groupId ->
                        val group = state.groups.find { it.id == groupId }
                        vm.loadGroup(groupId)
                        if (group != null) {
                            appNavigator.navigateRoot(
                                AppRoute.Group(
                                    groupId = groupId,
                                    groupName = group.name
                                )
                            )
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

/**
 * Retorna true se o displayname for genérico (começa com "user_") ou nulo.
 * Nesse caso o usuário ainda não definiu um apelido próprio.
 */
private fun isGenericNickname(displayname: String?): Boolean {
    if (displayname == null) return true
    return displayname.startsWith("user_")
}