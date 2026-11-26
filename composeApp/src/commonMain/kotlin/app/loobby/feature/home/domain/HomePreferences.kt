package app.loobby.feature.home.domain

/** O que está escolhido na barra lateral do Início. */
sealed interface HomeSelection {
    data object Lobby : HomeSelection
    data class Group(val groupId: String) : HomeSelection
}

/** O que o Início lembra entre uma abertura e outra do app. */
interface HomePreferences {
    var selection: HomeSelection
    var isRailOpen: Boolean
}
