package app.loobby.feature.library.domain.model

/** De qual biblioteca vêm os jogos da escolha rápida. */
sealed interface LibrarySource {
    /** A biblioteca da pessoa: a da gameplay única. */
    data object Mine : LibrarySource

    /** A biblioteca do grupo: a da gameplay do grupo. */
    data class Group(val groupId: String) : LibrarySource
}
