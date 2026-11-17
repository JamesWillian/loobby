package app.loobby.feature.groups.ui

import androidx.compose.runtime.*
import app.loobby.core.analytics.AppScreen
import app.loobby.core.analytics.TrackScreen
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

    // Conta a tela aberta. A chave leva o id só para distinguir um grupo ou evento
    // do outro; ela não sai do aparelho.
    appNavigator.current.let { TrackScreen(screen = it.screen, key = it.screenKey) }

    when (val route = appNavigator.current) {

        is AppRoute.Welcome -> {
            WelcomeScreen(
                isAnonymous = authState.isAnonymous,
                onCreateGroup = onCreateGroup,
                onJoinGroup = onJoinGroup,
                onInstantEvent = onInstantEvent,
                onLogin = onLogin,
                // Quando logado sem grupos, a Welcome vira empty-state e usa o nome
                // pra deixar claro que o login funcionou (sem isso parece que voltou
                // pra mesma tela).
                displayName = authState.profile?.displayname?.takeIf { !authState.isAnonymous },
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

private val AppRoute.screen: AppScreen
    get() = when (this) {
        is AppRoute.Welcome -> AppScreen.WELCOME
        is AppRoute.Group -> AppScreen.GROUP
        is AppRoute.GroupDetail -> AppScreen.GROUP_DETAIL
        is AppRoute.EventDetail -> AppScreen.EVENT_DETAIL
        is AppRoute.Teams -> AppScreen.TEAMS
        is AppRoute.TeamsReport -> AppScreen.TEAMS_REPORT
    }

/**
 * Identifica a abertura da tela. Fica de fora o nome do grupo ou do evento, que chega
 * depois do id e renavega para a mesma tela: seria contada duas vezes.
 */
private val AppRoute.screenKey: String
    get() = when (this) {
        is AppRoute.Welcome -> screen.id
        is AppRoute.Group -> "${screen.id}:$groupId"
        is AppRoute.GroupDetail -> "${screen.id}:$groupId"
        is AppRoute.EventDetail -> "${screen.id}:$eventId"
        is AppRoute.Teams -> "${screen.id}:$eventId"
        is AppRoute.TeamsReport -> "${screen.id}:$eventId"
    }
