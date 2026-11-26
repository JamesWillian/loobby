package app.loobby.feature.gameplays.domain.model

import kotlin.time.Instant

/** Um item das listas: o Lobby, as gameplays de um grupo e o Histórico. */
data class GameplaySummary(
    val id: String,
    val groupId: String?,
    val groupName: String?,
    val name: String,
    val mode: GameplayMode,
    val platforms: List<Platform>,
    val scheduledAt: Instant,
    val autoEndAt: Instant,
    val status: GameplayStatus,
    val endedAt: Instant?,
    val creator: GameplayUser,
    val myStatus: ParticipationStatus,
    val goingCount: Int,
    /** Os primeiros de quem vai; null para quem não tem foto. */
    val goingAvatars: List<String?>,
    /** Jogo definido: o jogo. */
    val game: GameInfo?,
    /**
     * Votação: os finalistas da rodada aberta, do mais votado para o menos. Enquanto o
     * desempate não tem voto, os da rodada anterior.
     */
    val leading: List<GameInfo>,
    /** Votação com 1 finalista: [leading] é um empate no 1º lugar. */
    val tie: Boolean,
) {
    val isSingle: Boolean get() = groupId == null
}
