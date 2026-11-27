package app.loobby.feature.gameplays.domain.repository

import app.loobby.feature.gameplays.domain.model.CommentsPage
import app.loobby.feature.gameplays.domain.model.GameChoice
import app.loobby.feature.gameplays.domain.model.Gameplay
import app.loobby.feature.gameplays.domain.model.GameplayChanges
import app.loobby.feature.gameplays.domain.model.GameplayComment
import app.loobby.feature.gameplays.domain.model.GameplayDraft
import app.loobby.feature.gameplays.domain.model.GameplayInvitePreview
import app.loobby.feature.gameplays.domain.model.GameplaySummary
import app.loobby.feature.gameplays.domain.model.GameplaysPage
import app.loobby.feature.gameplays.domain.model.ParticipationStatus
import kotlinx.coroutines.flow.Flow

/**
 * As gameplays da 2.0 (rotas /v2). As leituras caem no cache quando não há rede;
 * as escritas exigem rede e lançam OfflineException sem ela. Toda escrita sobre
 * uma gameplay devolve a gameplay inteira, já atualizada.
 */
interface GameplaysRepository {

    /**
     * O id de cada gameplay que sai deste aparelho: excluída, a pessoa saiu dela,
     * ou a API respondeu 404 (alguém excluiu, ou a pessoa perdeu o acesso). Ela já
     * saiu do cache; as listas abertas a tiram na hora, sem esperar a próxima carga.
     */
    val removals: Flow<String>

    /** O Lobby: as gameplays ativas dos grupos da pessoa e as únicas em que entrou. */
    suspend fun lobby(): List<GameplaySummary>

    /** As ativas de um grupo. */
    suspend fun activeInGroup(groupId: String): List<GameplaySummary>

    /**
     * As encerradas de um grupo (as Finalizadas), da mais recente; [before] pede as
     * anteriores a ela. Só a primeira página fica no cache.
     */
    suspend fun endedInGroup(groupId: String, before: String? = null): GameplaysPage

    /** O Histórico: as encerradas em que a pessoa marcou Vou, em páginas como [endedInGroup]. */
    suspend fun history(before: String? = null): GameplaysPage

    suspend fun gameplay(id: String): Gameplay

    suspend fun create(draft: GameplayDraft): Gameplay

    suspend fun update(id: String, changes: GameplayChanges): Gameplay

    suspend fun end(id: String): Gameplay

    /**
     * Exclui a gameplay, ativa ou encerrada, para todo mundo. Se a API diz que ela
     * já não existe (404), conta como excluída.
     */
    suspend fun delete(id: String)

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
