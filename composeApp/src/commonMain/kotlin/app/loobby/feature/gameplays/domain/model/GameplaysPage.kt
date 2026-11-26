package app.loobby.feature.gameplays.domain.model

/** Uma página das encerradas (Histórico ou Finalizadas do grupo), da mais recente. */
data class GameplaysPage(
    val gameplays: List<GameplaySummary>,
    /** Veio a página cheia: pode haver mais antigas. */
    val mayHaveMore: Boolean,
)
