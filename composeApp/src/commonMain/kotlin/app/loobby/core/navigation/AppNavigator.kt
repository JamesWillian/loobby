package app.loobby.core.navigation

import androidx.compose.runtime.*

class AppNavigator {
    var current by mutableStateOf<AppRoute>(AppRoute.InstantEvents)
        private set

    fun navigate(route: AppRoute) {
        current = route
    }
}

@Composable
fun rememberAppNavigator() = remember { AppNavigator() }