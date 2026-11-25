package app.loobby.feature.gameplays.data.model

import kotlinx.serialization.Serializable

// Os corpos das rotas /v2. O PATCH não está aqui: ele precisa mandar só os campos
// que mudaram (ausente ≠ null na API), e é montado no GameplaysApiImpl.

@Serializable
data class GameChoiceRequest(
    val gameId: String? = null,
    val name: String,
)

@Serializable
data class VotingConfigRequest(
    val finalists: Int,
    val multiVote: Boolean,
)

/** POST /v2/gameplays e POST /v2/groups/{groupId}/gameplays. */
@Serializable
data class CreateGameplayRequest(
    val name: String,
    val description: String? = null,
    val mode: String,
    val platforms: List<String>,
    /** RFC 3339, em UTC. */
    val scheduledAt: String,
    val game: GameChoiceRequest? = null,
    val voting: VotingConfigRequest? = null,
    val games: List<GameChoiceRequest>? = null,
)

@Serializable
data class RespondRequest(val status: String)

@Serializable
data class VotesRequest(val votes: List<String>)

@Serializable
data class CommentRequest(val body: String)

@Serializable
data class ReportRequest(val reason: String? = null)
