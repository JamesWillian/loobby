package app.loobby.feature.groups.ui

import androidx.compose.runtime.*
import app.loobby.core.navigation.*
import app.loobby.feature.auth.presentation.AuthScreen
import app.loobby.feature.groups.presentation.*

@Composable
fun AppContent(appNavigator: AppNavigator) {
    when (val route = appNavigator.current) {

        is AppRoute.Group -> {
            GroupScreen(
                groupId = route.groupId,
                groupName = route.groupName,
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
                    // GroupsViewModel.leave() is called inside GroupDetailScreen
                    // before invoking onLeaveGroup, handled below
                }
            )
        }
    }
}