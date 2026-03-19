package app.loobby.feature.groups.ui

import androidx.compose.runtime.*
import app.loobby.core.navigation.*
import app.loobby.feature.events.presentation.EventDetailScreen
import app.loobby.feature.groups.presentation.*

@Composable
fun AppContent(appNavigator: AppNavigator) {
    when (val route = appNavigator.current) {

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
                onBack = { appNavigator.popBack() }
            )
        }
    }
}