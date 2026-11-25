package app.loobby.core.navigation

/** As abas da barra de baixo. Cada uma tem a própria pilha de telas. */
enum class AppTab(val root: AppRoute) {
    HOME(AppRoute.Lobby),
    HISTORY(AppRoute.History),
    PROFILE(AppRoute.Profile),
}
