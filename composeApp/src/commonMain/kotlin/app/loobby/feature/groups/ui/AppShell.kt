package app.loobby.feature.groups.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import app.loobby.core.navigation.*
import app.loobby.feature.auth.presentation.AuthScreen
import app.loobby.feature.auth.presentation.AuthViewModel
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

    // Controla exibição da tela de Auth (Login / Register)
    var showAuthScreen by remember { mutableStateOf(false) }

    // Checa se o usuário é anônimo para decidir ação do botão de perfil
    val authState by authVm.uiState.collectAsState()

    if (showAuthScreen) {
        AuthScreen(
            onDismiss = {
                showAuthScreen = false
                vm.refreshMyGroups()
            }
        )
    } else {
        LaunchedEffect(state.selectedGroup) {
            state.selectedGroup?.let { group ->
                appNavigator.navigate(AppRoute.Group(group.id, group.name))
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
                        // Se anônimo → abrir tela de auth
                        // Se logado → navegar para perfil

                        showAuthScreen = true

                        // TODO: quando implementar checagem de isAnonymous,
                        //  trocar para navegar ao perfil se já registrado:
                        //  if (isAnonymous) showAuthScreen = true
                        //  else rootNavigator.navigate(RootRoute.Profile)
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
                        appNavigator.navigate(AppRoute.login)
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
}