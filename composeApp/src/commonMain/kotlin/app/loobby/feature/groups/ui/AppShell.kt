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

    // ── Auth state ──────────────────────────────────────────────────
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
            onDismiss = {
                showAuthSheet = false
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

    // ── Auto-navigate when selectedGroup changes ────────────────────
    LaunchedEffect(state.selectedGroup) {
        state.selectedGroup?.let { group ->
            appNavigator.navigateRoot(AppRoute.Group(group.id, group.name))
        }
    }

    // ── Main content ────────────────────────────────────────────────
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
                        showAuthSheet = true
                    } else {
                        showProfileSheet = true
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

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 16.dp))
            ) {
                AppContent(appNavigator)
            }
        }
    }
}