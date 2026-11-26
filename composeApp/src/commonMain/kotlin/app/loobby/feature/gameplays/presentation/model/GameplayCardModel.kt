package app.loobby.feature.gameplays.presentation.model

import app.loobby.feature.gameplays.domain.model.GameInfo
import app.loobby.feature.gameplays.domain.model.Gameplay
import app.loobby.feature.gameplays.domain.model.GameplayMode
import app.loobby.feature.gameplays.domain.model.GameplayStatus
import app.loobby.feature.gameplays.domain.model.GameplaySummary
import app.loobby.feature.gameplays.domain.model.ParticipationStatus
import app.loobby.feature.gameplays.domain.model.Platform
import kotlin.time.Instant

/**
 * O que um card de gameplay mostra. Sai do resumo das listas ou, quando a tela já
 * tem, da gameplay inteira; assim o card do grupo se atualiza depois de um voto.
 */
data class GameplayCardModel(
    val id: String,
    val name: String,
    val groupName: String?,
    val scheduledAt: Instant,
    val platform: Platform?,
    val mode: GameplayMode,
    val myStatus: ParticipationStatus,
    val goingCount: Int,
    val goingAvatars: List<String?>,
    val coverUrl: String?,
    val highlight: ModeHighlight,
    val isEnded: Boolean,
)

/** A linha que resume o jogo da gameplay. */
sealed interface ModeHighlight {
    data class Game(val name: String) : ModeHighlight
    data class Leading(val names: List<String>, val isTie: Boolean) : ModeHighlight
    data object NoVotes : ModeHighlight
    data object Free : ModeHighlight
}

fun GameplaySummary.toCardModel(): GameplayCardModel = GameplayCardModel(
    id = id,
    name = name,
    groupName = groupName,
    scheduledAt = scheduledAt,
    platform = platforms.firstOrNull(),
    mode = mode,
    myStatus = myStatus,
    goingCount = goingCount,
    goingAvatars = goingAvatars,
    coverUrl = game?.coverUrl ?: leading.firstNotNullOfOrNull(GameInfo::coverUrl),
    highlight = when (mode) {
        GameplayMode.FIXED -> game?.let { ModeHighlight.Game(it.name) } ?: ModeHighlight.Free
        GameplayMode.FREE -> ModeHighlight.Free
        GameplayMode.VOTING -> leadingOf(leading.map(GameInfo::name), isTie = tie)
    },
    isEnded = status == GameplayStatus.ENDED,
)

fun Gameplay.toCardModel(): GameplayCardModel {
    val v = voting
    val finalists = v?.let { it.games.filter(it::isFinalist) }.orEmpty()
    // O desempate sem voto mostra o empate que o abriu, como as listas da API.
    val openTiebreak = v != null && v.isTiebreak && finalists.isEmpty()
    val leading = if (openTiebreak) v.candidates else finalists
    return GameplayCardModel(
        id = id,
        name = name,
        groupName = groupName,
        scheduledAt = scheduledAt,
        platform = platform,
        mode = mode,
        myStatus = myStatus,
        goingCount = counts.going,
        goingAvatars = goingAvatars,
        coverUrl = game?.coverUrl ?: leading.firstNotNullOfOrNull { it.coverUrl },
        highlight = when (mode) {
            GameplayMode.FIXED -> game?.let { ModeHighlight.Game(it.name) } ?: ModeHighlight.Free
            GameplayMode.FREE -> ModeHighlight.Free
            GameplayMode.VOTING -> leadingOf(leading.map { it.name }, isTie = openTiebreak || v?.result?.tie == true)
        },
        isEnded = isEnded,
    )
}

private fun leadingOf(names: List<String>, isTie: Boolean): ModeHighlight =
    if (names.isEmpty()) ModeHighlight.NoVotes else ModeHighlight.Leading(names, isTie)
