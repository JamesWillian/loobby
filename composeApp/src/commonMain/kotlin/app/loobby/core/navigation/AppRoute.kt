package app.loobby.core.navigation

sealed class AppRoute {
    /**
     * Tela principal de um grupo: exibe eventos, filtros e ações do grupo.
     */
    data class Group(
        val groupId: String = "",
        val groupName: String = ""
    ) : AppRoute()

    data class GroupDetail(val groupId: String) : AppRoute()

    data class EventDetail(
        val eventId: String,
        val eventName: String
    ) : AppRoute()

    data class Teams(
        val eventId: String
    ) : AppRoute()
}