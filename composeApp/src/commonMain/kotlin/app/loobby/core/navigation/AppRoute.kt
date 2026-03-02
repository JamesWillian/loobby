package app.loobby.core.navigation

sealed interface AppRoute {
    data class Group(val groupId: String) : AppRoute
    data object InstantEvents : AppRoute
}