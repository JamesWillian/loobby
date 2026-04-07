package app.loobby.core.navigation

import androidx.compose.runtime.*

class AppNavigator(initialRoute: AppRoute = AppRoute.Welcome) {
    private val backStack = mutableStateListOf<AppRoute>(initialRoute)

    val current: AppRoute get() = backStack.last()

    fun navigate(route: AppRoute) {
        backStack.add(route)
    }

    fun popBack(): Boolean {
        if (backStack.size > 1) {
            backStack.removeLast()
            return true
        }
        return false
    }

    val canPopBack: Boolean get() = backStack.size > 1

    /** Substitui toda a pilha (útil para trocar de grupo sem acumular histórico). */
    fun navigateRoot(route: AppRoute) {
        backStack.clear()
        backStack.add(route)
    }
}

@Composable
fun rememberAppNavigator(initialRoute: AppRoute = AppRoute.Welcome) =
    remember {
        AppNavigator(initialRoute)
    }