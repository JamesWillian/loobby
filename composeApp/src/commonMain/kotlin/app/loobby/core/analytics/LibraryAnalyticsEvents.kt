package app.loobby.core.analytics

// Os eventos das bibliotecas de jogos. Como os outros, levam só valores fixos: nunca
// o jogo, o grupo ou a pessoa.

data object LibraryGameAdded : AnalyticsEvent("library_game_added")

data object LibraryGameRemoved : AnalyticsEvent("library_game_removed")

/** O "tenho o jogo" mudou; [owned] diz se ficou com alguma plataforma marcada. */
data class LibraryOwnedChanged(val owned: Boolean) : AnalyticsEvent(
    name = "library_owned_changed",
    params = mapOf("owned" to owned.toString()),
)

data object GroupLibraryGameAdded : AnalyticsEvent("group_library_game_added")

data object GroupLibraryGameRemoved : AnalyticsEvent("group_library_game_removed")

/** De onde veio o jogo escolhido para a gameplay. */
enum class GamePickSource(val id: String) {
    /** A busca no catálogo. */
    CATALOG("catalog"),

    /** O nome digitado. */
    MANUAL("manual"),

    /** A escolha rápida, da biblioteca do grupo. */
    GROUP_LIBRARY("group_library"),

    /** A escolha rápida, da biblioteca da pessoa. */
    MY_LIBRARY("my_library"),
}
