package app.loobby.feature.groups.ui

import androidx.compose.runtime.*
import app.loobby.core.navigation.*
import app.loobby.feature.auth.presentation.AuthViewModel
import app.loobby.feature.events.presentation.EventDetailScreen
import app.loobby.feature.events.teams.presentation.TeamsReportScreen
import app.loobby.feature.events.teams.presentation.TeamsScreen
import app.loobby.feature.events.teams.presentation.TeamsViewModel
import app.loobby.feature.groups.presentation.*
import org.koin.compose.koinInject

@Composable
fun AppContent(
    appNavigator: AppNavigator,
    onCreateGroup: () -> Unit = {},
    onJoinGroup: () -> Unit = {},
    onInstantEvent: () -> Unit = {},
    onLogin: () -> Unit = {},
) {
    val authVm: AuthViewModel = koinInject()
    val authState by authVm.uiState.collectAsState()

    when (val route = appNavigator.current) {

        is AppRoute.Welcome -> {
            WelcomeScreen(
                isAnonymous = authState.isAnonymous,
                onCreateGroup = onCreateGroup,
                onJoinGroup = onJoinGroup,
                onInstantEvent = onInstantEvent,
                onLogin = onLogin,
            )
        }

        is AppRoute.Group -> {
            GroupScreen(
                groupId = route.groupId,
                groupName = route.groupName,
                onEventClick = { eventId, eventName ->
                    appNavigator.navigate(
                        AppRoute.EventDetail(
                            eventId = eventId,
                            eventName = eventName,
                            groupId = route.groupId
                        )
                    )
                },
                onGroupNameClick = {
                    appNavigator.navigate(AppRoute.GroupDetail(route.groupId))
                }
            )
        }

        is AppRoute.GroupDetail -> {
            GroupDetailScreen(
                groupId = route.groupId,
                onBack = { appNavigator.popBack() },
                onLeaveGroup = {
                    appNavigator.popBack()
                }
            )
        }

        is AppRoute.EventDetail -> {
            // Se EventDetail é a rota root (evento instantâneo aberto pelo sidebar),
            // não existe rota anterior — então escondemos o botão de voltar e mostramos
            // o título "Evento Rápido" no lugar. Se foi empilhado (a partir de um grupo),
            // mantemos o comportamento padrão: botão de voltar + popBack.
            val isRootRoute = !appNavigator.canPopBack
            EventDetailScreen(
                eventId = route.eventId,
                onBack = {
                    if (appNavigator.canPopBack) {
                        appNavigator.popBack()
                    }
                },
                onOpenTeams = {
                    appNavigator.navigate(AppRoute.Teams(route.eventId, route.eventName))
                },
                onOpenTeamsReport = {
                    appNavigator.navigate(AppRoute.TeamsReport(route.eventId, route.eventName))
                },
                showBackButton = !isRootRoute
            )
        }

        is AppRoute.Teams -> {
            TeamsScreen(
                eventId = route.eventId,
                eventName = route.eventName,
                onBack = { appNavigator.popBack() }
            )
        }

        is AppRoute.TeamsReport -> {
            // Tela "Formação dos Times" aberta diretamente para usuários comuns.
            // Carrega os times via TeamsViewModel e renderiza a versão somente leitura.
            // O popBack volta direto para EventDetail (sem passar pelo TeamsScreen).
            val teamsVm: TeamsViewModel = koinInject()
            val teamsState by teamsVm.uiState.collectAsState()
            LaunchedEffect(route.eventId) { teamsVm.load(route.eventId) }
            TeamsReportScreen(
                eventName = route.eventName,
                teams = teamsState.teams,
                onBack = { appNavigator.popBack() }
            )
        }
    }
}