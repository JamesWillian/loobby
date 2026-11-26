package app.loobby.feature.gameplays.domain.model

import kotlin.time.Instant

/** Uma gameplay nova, já validada pelo formulário. [groupId] null = única. */
data class GameplayDraft(
    val groupId: String?,
    val name: String,
    val description: String?,
    val mode: GameplayMode,
    val platform: Platform,
    val scheduledAt: Instant,
    /** Jogo definido. */
    val game: GameChoice? = null,
    /** Votação. */
    val voting: VotingConfig? = null,
    /** Votação: os jogos que já entram na cédula. */
    val games: List<GameChoice> = emptyList(),
)

data class VotingConfig(
    val finalists: Int,
    val multiVote: Boolean,
)

/**
 * O que mudou numa gameplay. Null = não muda. A descrição vazia apaga a que havia;
 * a configuração da votação só vale antes do primeiro voto.
 */
data class GameplayChanges(
    val name: String? = null,
    val description: String? = null,
    val scheduledAt: Instant? = null,
    val platform: Platform? = null,
    val game: GameChoice? = null,
    val voting: VotingConfig? = null,
) {
    val isEmpty: Boolean
        get() = name == null && description == null && scheduledAt == null &&
            platform == null && game == null && voting == null
}
