package app.loobby.feature.groups.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import app.loobby.core.navigation.*
import app.loobby.feature.auth.presentation.AuthScreen
import app.loobby.feature.auth.presentation.AuthViewModel
import app.loobby.feature.auth.presentation.ProfileHost
import app.loobby.feature.events.presentation.CreateEventSheet
import app.loobby.feature.groups.presentation.GroupsViewModel
import org.koin.compose.koinInject

@Composable
fun AppShell(
    rootNavigator: RootNavigator,
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

    // ── Fullscreen overlays ─────────────────────────────────────────
    var showAuthScreen by remember { mutableStateOf(false) }
    var showProfileScreen by remember { mutableStateOf(false) }

    // ── Auth state (fonte única de isAnonymous) ─────────────────────
    val authState by authVm.uiState.collectAsState()

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
                    appNavigator.navigate(AppRoute.Group(groupId, groupName))
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
                    appNavigator.navigate(AppRoute.Group(groupId, groupName))
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

    // ── Fullscreen: Auth (login/register) ───────────────────────────
    if (showAuthScreen) {
        AuthScreen(
            onDismiss = {
                showAuthScreen = false
                vm.refreshMyGroups()
            }
        )
        return
    }

    // ── Fullscreen: Profile ─────────────────────────────────────────
    if (showProfileScreen) {
        ProfileHost(
            onDismiss = {
                showProfileScreen = false
            }
        )
        return
    }

    // ── Main content ────────────────────────────────────────────────
    LaunchedEffect(state.selectedGroup) {
        state.selectedGroup?.let { group ->
            appNavigator.navigateRoot(AppRoute.Group(group.id, group.name))
        }
    }

    Scaffold { innerPadding ->
        Row(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            GroupSidebar(
                isLoading = state.isLoading,
                groups = state.groups,
                selectedGroupId = state.selectedGroup?.id,
                userAvatarUrl = authState.profile?.avatarUrl,
                onProfileClick = {
                    if (authState.isAnonymous) {
                        showAuthScreen = true
                    } else {
                        showProfileScreen = true
                    }
                },
                onGroupSelected = { groupId ->
                    val group = state.groups.find { it.id == groupId }
                    vm.loadGroup(groupId)
                    if (group != null) {
                        appNavigator.navigate(
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

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                AppContent(appNavigator)
            }
        }
    }
}