package app.loobby.feature.gameplays.data.model

import kotlinx.serialization.Serializable

// As respostas das rotas /v2, como a API manda. Os campos com valor padrão são os
// que podem faltar ou vir null; o mapper converte tudo para o domínio.

@Serializable
data class UserRefResponse(
    val id: String,
    val username: String = "",
    val displayname: String = "",
    val avatarUrl: String? = null,
)

@Serializable
data class GameRefResponse(
    val gameId: String? = null,
    val name: String,
    val coverUrl: String? = null,
    /** Só no jogo definido do detalhe: quem vai e tem o jogo (o match). */
    val owners: List<UserRefResponse> = emptyList(),
)

@Serializable
data class PermissionsResponse(
    val respond: Boolean = false,
    val vote: Boolean = false,
    val editGames: Boolean = false,
    val comment: Boolean = false,
    val manage: Boolean = false,
    val configureVoting: Boolean = false,
    val tiebreak: Boolean = false,
    val leave: Boolean = false,
    val moderate: Boolean = false,
    /** Uma API de antes do Excluir não manda o campo: o botão não aparece. */
    val delete: Boolean = false,
)

@Serializable
data class CountsResponse(
    val going: Int = 0,
    val notGoing: Int = 0,
    val pending: Int = 0,
)

@Serializable
data class ParticipantResponse(
    val user: UserRefResponse,
    val status: String,
    val isAnonymous: Boolean = false,
)

@Serializable
data class RoundVotesResponse(
    val round: Int,
    val votes: Int = 0,
    val voters: List<UserRefResponse> = emptyList(),
)

@Serializable
data class VotingGameResponse(
    val id: String,
    val gameId: String? = null,
    val name: String,
    val coverUrl: String? = null,
    val addedBy: String? = null,
    val candidate: Boolean = false,
    val myVote: Boolean = false,
    val rounds: List<RoundVotesResponse> = emptyList(),
    /** Quem vai e tem o jogo na biblioteca, numa plataforma da gameplay (o match). */
    val owners: List<UserRefResponse> = emptyList(),
)

@Serializable
data class VotingResultResponse(
    val round: Int = 1,
    val finalistIds: List<String> = emptyList(),
    val tie: Boolean = false,
)

@Serializable
data class VotingResponse(
    val finalists: Int = 1,
    val multiVote: Boolean = false,
    val locked: Boolean = false,
    val round: Int = 1,
    val games: List<VotingGameResponse> = emptyList(),
    val result: VotingResultResponse = VotingResultResponse(),
)

/** GET /v2/gameplays/{id}, e a resposta de toda ação sobre a gameplay. */
@Serializable
data class GameplayResponse(
    val id: String,
    val groupId: String? = null,
    val groupName: String? = null,
    val name: String,
    val description: String? = null,
    val mode: String,
    val platforms: List<String> = emptyList(),
    val scheduledAt: String,
    val autoEndAt: String,
    val status: String,
    val endedAt: String? = null,
    val endedBy: UserRefResponse? = null,
    val inviteCode: String? = null,
    val creator: UserRefResponse,
    val createdAt: String,
    val myStatus: String,
    val permissions: PermissionsResponse = PermissionsResponse(),
    val counts: CountsResponse = CountsResponse(),
    val participants: List<ParticipantResponse> = emptyList(),
    val game: GameRefResponse? = null,
    val voting: VotingResponse? = null,
)

/** Um item do Lobby, das gameplays do grupo e do Histórico. */
@Serializable
data class GameplaySummaryResponse(
    val id: String,
    val groupId: String? = null,
    val groupName: String? = null,
    val name: String,
    val mode: String,
    val platforms: List<String> = emptyList(),
    val scheduledAt: String,
    val autoEndAt: String,
    val status: String,
    val endedAt: String? = null,
    val creator: UserRefResponse,
    val myStatus: String,
    val goingCount: Int = 0,
    val goingAvatars: List<String?> = emptyList(),
    val game: GameRefResponse? = null,
    val leading: List<GameRefResponse> = emptyList(),
    /** Votação com 1 finalista e empate no 1º lugar. */
    val tie: Boolean = false,
)

/** GET /v2/invites/{code}. */
@Serializable
data class InvitePreviewResponse(
    val id: String,
    val name: String,
    val mode: String,
    val platforms: List<String> = emptyList(),
    val scheduledAt: String,
    val autoEndAt: String,
    val status: String,
    val endedAt: String? = null,
    val creator: UserRefResponse,
    val goingCount: Int = 0,
    val joined: Boolean = false,
)

@Serializable
data class CommentResponse(
    val id: String,
    val gameplayId: String,
    val author: UserRefResponse,
    val body: String,
    val createdAt: String,
    val canDelete: Boolean = false,
    val reportedByMe: Boolean = false,
)
