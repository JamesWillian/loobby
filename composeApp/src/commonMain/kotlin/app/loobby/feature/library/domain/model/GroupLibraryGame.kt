package app.loobby.feature.library.domain.model

import app.loobby.feature.gameplays.domain.model.GameplayUser
import app.loobby.feature.gameplays.domain.model.Platform
import kotlin.time.Instant

/** Um jogo da biblioteca do grupo: os jogos que o grupo joga. */
data class GroupLibraryGame(
    val catalogId: String,
    val name: String,
    val coverUrl: String?,
    /** Onde o jogo existe, pelo catálogo. Vazia: o catálogo não diz. */
    val availablePlatforms: List<Platform>,
    /** Null: a conta de quem adicionou foi excluída. */
    val addedBy: GameplayUser?,
    val addedAt: Instant,
)
