package app.loobby.core.navigation

/**
 * As telas do app. As raízes do Início são o [Lobby] e o [GroupHome], trocadas pela
 * barra lateral; as outras abas têm uma raiz fixa. O resto é empilhado por cima.
 */
sealed interface AppRoute {

    /** As gameplays ativas da pessoa, de todos os grupos e as únicas. */
    data object Lobby : AppRoute

    /** O Início de um grupo: as gameplays ativas, com a votação no card. */
    data class GroupHome(val groupId: String, val groupName: String = "") : AppRoute

    /** Membros, convite, biblioteca e configurações do grupo. */
    data class GroupDetail(val groupId: String) : AppRoute

    /** As Finalizadas do grupo: as gameplays encerradas, da mais recente. */
    data class EndedGameplays(val groupId: String, val groupName: String = "") : AppRoute

    data class GameplayDetail(val gameplayId: String) : AppRoute

    /** As encerradas em que a pessoa marcou Vou. */
    data object History : AppRoute

    /** A conta e a biblioteca de jogos. */
    data object Profile : AppRoute
}

/** Lobby e grupo são raízes do Início: é ali que a barra lateral aparece. */
val AppRoute.isHomeRoot: Boolean
    get() = this is AppRoute.Lobby || this is AppRoute.GroupHome
