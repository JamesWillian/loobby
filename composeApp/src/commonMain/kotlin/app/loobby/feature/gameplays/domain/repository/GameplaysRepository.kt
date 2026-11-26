package app.loobby.feature.gameplays.domain.repository

import app.loobby.feature.gameplays.domain.model.CommentsPage
import app.loobby.feature.gameplays.domain.model.GameChoice
import app.loobby.feature.gameplays.domain.model.Gameplay
import app.loobby.feature.gameplays.domain.model.GameplayChanges
import app.loobby.feature.gameplays.domain.model.GameplayComment
import app.loobby.feature.gameplays.domain.model.GameplayDraft
import app.loobby.feature.gameplays.domain.model.GameplayInvitePreview
import app.loobby.feature.gameplays.domain.model.GameplaySummary
import app.loobby.feature.gameplays.domain.model.ParticipationStatus

/**
 * As gameplays da 2.0 (rotas /v2). As leituras caem no cache quando não há rede;
 * as escritas exigem rede e lançam OfflineException sem ela. Toda escrita sobre
 * uma gameplay devolve a gameplay inteira, já atualizada.
 */
interface GameplaysRepository {

    /** O Lobby: as gameplays ativas dos grupos da pessoa e as únicas em que entrou. */
    suspend fun lobby(): List<GameplaySummary>

    /** As ativas de um grupo. */
    suspend fun activeInGroup(groupId: String): List<GameplaySummary>

    suspend fun gameplay(id: String): Gameplay

    suspend fun create(draft: GameplayDraft): Gameplay

    suspend fun update(id: String, changes: GameplayChanges): Gameplay

    suspend fun end(id: String): Gameplay

    suspend fun respond(id: String, status: ParticipationStatus): Gameplay

    /** Troca os votos da pessoa na rodada aberta. Lista vazia tira o voto. */
    suspend fun vote(id: String, votingGameIds: List<String>): Gameplay

    suspend fun addVotingGame(id: String, game: GameChoice): Gameplay

    suspend fun removeVotingGame(id: String, votingGameId: String): Gameplay

    suspend fun openTiebreak(id: String): Gameplay

    /** Sai da gameplay única. */
    suspend fun leave(id: String)

    suspend fun invitePreview(code: String): GameplayInvitePreview

    /** Entra na única pelo convite, como Pendente. */
    suspend fun joinByInvite(code: String): Gameplay

    /** Os comentários, do mais novo para o mais antigo; [before] pede os anteriores a ele. */
    suspend fun comments(gameplayId: String, before: String? = null): CommentsPage

    suspend fun postComment(gameplayId: String, body: String): GameplayComment

    suspend fun deleteComment(gameplayId: String, commentId: String)

    suspend fun reportComment(gameplayId: String, commentId: String, reason: String?)
}
