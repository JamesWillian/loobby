package app.loobby.feature.gameplays.domain.model

import kotlin.time.Instant

/** O que o convite de uma gameplay única mostra antes de a pessoa entrar. */
data class GameplayInvitePreview(
    val id: String,
    val name: String,
    val mode: GameplayMode,
    val platforms: List<Platform>,
    val scheduledAt: Instant,
    val status: GameplayStatus,
    val creator: GameplayUser,
    val goingCount: Int,
    /** A pessoa já está nela: o botão vira "Abrir". */
    val joined: Boolean,
) {
    val platform: Platform? get() = platforms.firstOrNull()
    val isEnded: Boolean get() = status == GameplayStatus.ENDED
}
