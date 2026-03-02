package app.loobby.core.navigation

sealed interface RootRoute {
    data object App : RootRoute
    data object Profile : RootRoute
}