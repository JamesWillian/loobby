package app.loobby.feature.groups.ui

import androidx.compose.runtime.*
import app.loobby.core.navigation.*
import app.loobby.feature.auth.presentation.AuthViewModel
import app.loobby.feature.events.presentation.EventDetailScreen
import app.loobby.feature.events.teams.presentation.TeamsScreen
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
                    appNavigator.navigate(AppRoute.EventDetail(eventId, eventName))
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
            EventDetailScreen(
                eventId = route.eventId,
                onBack = {
                    // Se EventDetail é a rota root (evento inst. da sidebar), back não faz nada
                    // Se foi navegado via push (de dentro de um grupo), faz popBack normal
                    if (appNavigator.canPopBack) {
                        appNavigator.popBack()
                    }
                },
                onOpenTeams = {
                    appNavigator.navigate(AppRoute.Teams(route.eventId, route.eventName))
                }
            )
        }

        is AppRoute.Teams -> {
            TeamsScreen(
                eventId = route.eventId,
                eventName = route.eventName,
                onBack = { appNavigator.popBack() }
            )
        }
    }
}