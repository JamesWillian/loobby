package app.loobby.feature.library.domain.model

/** Os limites que a API confere (409 quando passa). O app avisa antes. */
object LibraryRules {
    const val MAX_MINE = 500
    const val MAX_GROUP = 100
}
