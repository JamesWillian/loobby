package app.loobby.feature.gameplays.domain.model

import kotlin.time.Duration.Companion.minutes

/** Os limites que a API confere. O app confere antes, para avisar no campo. */
object GameplayRules {
    const val MAX_NAME = 100
    const val MAX_DESCRIPTION = 500
    const val MAX_GAME_NAME = 150
    const val MAX_GAMES = 20
    const val MAX_FINALISTS = 20
    const val MAX_COMMENT = 1000
    const val MAX_REPORT_REASON = 500
    const val COMMENTS_PAGE = 50

    /** Encerradas por página: o Histórico e as Finalizadas do grupo. */
    const val ENDED_PAGE = 20

    /** "Começar agora" chega um pouco no passado; a API aceita até 5 min. */
    val PAST_TOLERANCE = 5.minutes
}
