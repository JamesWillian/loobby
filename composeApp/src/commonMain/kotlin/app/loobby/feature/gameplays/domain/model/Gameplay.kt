package app.loobby.feature.gameplays.domain.model

import kotlin.time.Instant

/**
 * A gameplay inteira, como a tela de detalhe mostra. É também a resposta de toda
 * ação sobre ela: a tela troca o que tinha pelo que voltou.
 */
data class Gameplay(
    val id: String,
    val groupId: String?,
    val groupName: String?,
    val name: String,
    val description: String?,
    val mode: GameplayMode,
    val platforms: List<Platform>,
    val scheduledAt: Instant,
    val autoEndAt: Instant,
    val status: GameplayStatus,
    val endedAt: Instant?,
    /** Null com [endedAt] preenchido: encerrou sozinha. */
    val endedBy: GameplayUser?,
    /** Só na única: o convite para chamar gente. */
    val inviteCode: String?,
    val creator: GameplayUser,
    val createdAt: Instant,
    val myStatus: ParticipationStatus,
    val permissions: GameplayPermissions,
    val counts: ParticipationCounts,
    val participants: List<Participant>,
    /** Jogo definido: o jogo. */
    val game: GameInfo?,
    /** Votação: a cédula, os votos e o resultado. */
    val voting: Voting?,
    /** Jogo definido do catálogo: quem vai e tem o jogo na biblioteca (o match). */
    val gameOwners: List<GameplayUser> = emptyList(),
) {
    val isSingle: Boolean get() = groupId == null
    val isEnded: Boolean get() = status == GameplayStatus.ENDED
    val platform: Platform? get() = platforms.firstOrNull()

    /** Avatares de quem marcou Vou, na ordem em que responderam. */
    val goingAvatars: List<String?>
        get() = participants.filter { it.status == ParticipationStatus.GOING }.map { it.user.avatarUrl }
}

/** Os botões que quem lê pode usar. O servidor confere de novo cada ação. */
data class GameplayPermissions(
    val respond: Boolean,
    val vote: Boolean,
    val editGames: Boolean,
    val comment: Boolean,
    val manage: Boolean,
    val configureVoting: Boolean,
    val tiebreak: Boolean,
    val leave: Boolean,
    val moderate: Boolean,
    /** Excluir: os mesmos do Encerrar, e também na encerrada. */
    val delete: Boolean,
)

data class ParticipationCounts(
    val going: Int,
    val notGoing: Int,
    /** Quem pode responder e não respondeu (anônimo não conta). */
    val pending: Int,
)

data class Participant(
    val user: GameplayUser,
    val status: ParticipationStatus,
    val isAnonymous: Boolean,
)
