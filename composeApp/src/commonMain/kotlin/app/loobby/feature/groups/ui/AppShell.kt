package app.loobby.feature.groups.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import app.loobby.core.navigation.*

@Composable
fun AppShell(rootNavigator: RootNavigator) {

    val appNavigator = rememberAppNavigator()

    Scaffold() { innerPadding ->
        Row(Modifier
            .fillMaxSize()
            .padding(innerPadding)
        ) {

            GroupSidebar(
                onProfileClick = {
                    rootNavigator.navigate(RootRoute.Profile)
                },
                onGroupSelected = { groupId ->
                    appNavigator.navigate(AppRoute.Group(groupId))
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