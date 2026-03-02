package app.loobby.core.navigation

import androidx.compose.runtime.*

class RootNavigator {
    var current by mutableStateOf<RootRoute>(RootRoute.App)
        private set

    fun navigate(route: RootRoute) {
        current = route
    }
}

@Composable
fun rememberRootNavigator() = remember { RootNavigator() }