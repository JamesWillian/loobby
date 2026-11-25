package app.loobby.core.analytics

import app.loobby.feature.gameplays.domain.model.GameplayMode
import app.loobby.feature.gameplays.domain.model.ParticipationStatus
import app.loobby.feature.gameplays.domain.model.Platform

// Os eventos da 2.0. Como os da v1, levam só valores fixos escritos no código
// (modo, plataforma, de onde veio a ação), nunca nome, texto, código ou id.

data class GameplayCreated(
    val mode: GameplayMode,
    val inGroup: Boolean,
    val platform: Platform,
) : AnalyticsEvent(
    name = "gameplay_created",
    params = mapOf(
        "mode" to mode.id.lowercase(),
        "scope" to if (inGroup) "group" else "single",
        "platform" to platform.id.lowercase(),
    ),
)

data class GameplayUpdated(val mode: GameplayMode) : AnalyticsEvent(
    name = "gameplay_updated",
    params = mapOf("mode" to mode.id.lowercase()),
)

data object GameplayEnded : AnalyticsEvent("gameplay_ended")

data class GameplayResponded(val status: ParticipationStatus, val source: ActionSource) : AnalyticsEvent(
    name = "gameplay_response",
    params = mapOf("status" to status.id.lowercase(), "source" to source.id),
)

/** Votou, trocou ou tirou o voto ([cleared]). */
data class GameplayVoted(val source: ActionSource, val cleared: Boolean) : AnalyticsEvent(
    name = "gameplay_voted",
    params = mapOf("source" to source.id, "cleared" to cleared.toString()),
)

data class VotingGameAdded(val fromCatalog: Boolean) : AnalyticsEvent(
    name = "voting_game_added",
    params = mapOf("source" to if (fromCatalog) "catalog" else "manual"),
)

data object VotingGameRemoved : AnalyticsEvent("voting_game_removed")

data object TiebreakOpened : AnalyticsEvent("tiebreak_opened")

data object GameplayJoinedByInvite : AnalyticsEvent("gameplay_joined_by_invite")

data object GameplayLeft : AnalyticsEvent("gameplay_left")

data object CommentPosted : AnalyticsEvent("comment_posted")

data object CommentDeleted : AnalyticsEvent("comment_deleted")

data object CommentReported : AnalyticsEvent("comment_reported")

/** O app abriu por um push da 2.0. Vai só o tipo do push, entre os conhecidos. */
data class GameplayPushOpened(val pushType: String?) : AnalyticsEvent(
    name = "push_opened",
    params = mapOf("target" to "gameplay", "push_type" to knownPushType(pushType)),
)

/** De onde veio a ação: da tela da gameplay ou do card no Início do grupo. */
enum class ActionSource(val id: String) {
    DETAIL("gameplay_detail"),
    GROUP_CARD("group_card"),
}

private fun knownPushType(type: String?): String = when (type) {
    "GAMEPLAY_NEW_IN_GROUP" -> "new_in_group"
    "GAMEPLAY_TIEBREAK_OPENED" -> "tiebreak_opened"
    "GAMEPLAY_REMINDER" -> "reminder"
    "GAMEPLAY_TODAY_PENDING" -> "today_pending"
    else -> "other"
}
