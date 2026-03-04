package app.loobby.feature.groups.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import app.loobby.core.navigation.*
import app.loobby.feature.groups.presentation.GroupsViewModel
import org.koin.compose.koinInject

@Composable
fun AppShell(
    rootNavigator: RootNavigator,
    vm: GroupsViewModel = koinInject()
) {

    val state by vm.uiState.collectAsState()
    val appNavigator = rememberAppNavigator()

    Scaffold() { innerPadding ->
        Row(Modifier
            .fillMaxSize()
            .padding(innerPadding)
        ) {

            GroupSidebar(
                isLoading = state.isLoading,
                groups = state.groups,
                selectedGroupId = state.selectedGroup?.id,
                onProfileClick = {
                    rootNavigator.navigate(RootRoute.Profile)
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
//                    appNavigator.navigate(AppRoute.CreateGroup)
                }
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                AppContent(
                    appNavigator
//                    uiState = uiState,
//                    onJoin = { code -> viewModel.joinGroup(code) },
//                    onLeave = { groupId -> viewModel.leaveGroup(groupId) },
//                    onLoadMembers = { groupId -> viewModel.listMembers(groupId) }
                )
            }
        }
    }
}