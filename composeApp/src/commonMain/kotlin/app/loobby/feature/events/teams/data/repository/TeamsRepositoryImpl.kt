package app.loobby.feature.events.teams.data.repository

import app.loobby.core.network.ConnectivityObserver
import app.loobby.core.network.OfflineException
import app.loobby.db.LoobbyDatabase
import app.loobby.feature.events.teams.data.cache.toDomain
import app.loobby.feature.events.teams.data.mapper.toDomain
import app.loobby.feature.events.teams.data.model.*
import app.loobby.feature.events.teams.data.remote.TeamsApi
import app.loobby.feature.events.teams.domain.model.TeamDomain
import app.loobby.feature.events.teams.domain.model.TeamPlayerDomain
import app.loobby.feature.events.teams.domain.repository.TeamsRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Cache de times + jogadores. Segue o mesmo padrão dos demais repositórios:
 *  - leitura online: API → substitui o cache do evento → retorna o fresco.
 *  - leitura offline: retorna o cache (ou lista vazia se não houver nada).
 *  - escritas (create/update/delete/addPlayer/removePlayer/move/autoGenerate)
 *    exigem rede. Quando a API responde, atualizamos o cache com o resultado
 *    retornado (que é o estado pós-operação — o backend recomputa o time).
 */
class TeamsRepositoryImpl(
    private val api: TeamsApi,
    private val db: LoobbyDatabase,
    private val connectivity: ConnectivityObserver,
) : TeamsRepository {

    private val teamQueries = db.teamQueries
    private val playerQueries = db.teamPlayerQueries

    override suspend fun listTeams(eventId: String): List<TeamDomain> {
        if (!connectivity.isOnlineNow()) return cachedTeamsByEvent(eventId)
        return try {
            val fresh = api.listTeams(eventId).map { it.toDomain() }
            replaceTeamsForEvent(eventId, fresh)
            fresh
        } catch (t: Throwable) {
            cachedTeamsByEvent(eventId).ifEmpty { throw t }
        }
    }

    override suspend fun createTeam(
        eventId: String, name: String, color: String?, playerIds: List<String>
    ): TeamDomain {
        requireOnline()
        val request = CreateTeamRequest(
            name = name,
            color = color,
            players = playerIds.map { TeamPlayerRequest(userId = it) }
        )
        val created = api.createTeam(eventId, request).toDomain()
        upsertTeam(created)
        return created
    }

    override suspend fun updateTeam(
        eventId: String, teamId: String, name: String?, color: String?, order: Int?
    ): TeamDomain {
        requireOnline()
        val request = UpdateTeamRequest(name = name, color = color, order = order)
        val updated = api.updateTeam(eventId, teamId, request).toDomain()
        upsertTeam(updated)
        return updated
    }

    override suspend fun deleteTeam(eventId: String, teamId: String) {
        requireOnline()
        api.deleteTeam(eventId, teamId)
        db.transaction {
            playerQueries.clearByTeam(teamId)
            teamQueries.deleteById(teamId)
        }
    }

    override suspend fun addPlayer(
        eventId: String, teamId: String, userId: String
    ): TeamDomain {
        requireOnline()
        val request = AddPlayerToTeamRequest(userId = userId)
        val updated = api.addPlayer(eventId, teamId, request).toDomain()
        upsertTeam(updated)
        return updated
    }

    override suspend fun removePlayer(
        eventId: String, teamId: String, userId: String
    ): TeamDomain {
        requireOnline()
        val updated = api.removePlayer(eventId, teamId, userId).toDomain()
        upsertTeam(updated)
        return updated
    }

    override suspend fun movePlayer(
        eventId: String, fromTeamId: String, userId: String, toTeamId: String
    ): TeamDomain {
        requireOnline()
        val request = UpdateTeamPlayerRequest(newTeamId = toTeamId)
        val updated = api.updatePlayer(eventId, fromTeamId, userId, request).toDomain()
        // O backend remove o jogador do time de origem e coloca no destino.
        // A resposta é o time de destino — para manter o cache consistente,
        // apagamos a entrada no team origem e atualizamos o destino.
        db.transaction {
            // Remove explicitamente o jogador do time antigo no cache
            val sourceRows = playerQueries.selectByTeam(fromTeamId).executeAsList()
                .filter { it.user_id != userId }
            playerQueries.clearByTeam(fromTeamId)
            val now = nowMillis()
            sourceRows.forEachIndexed { idx, row ->
                playerQueries.upsert(
                    team_id = row.team_id,
                    user_id = row.user_id,
                    event_id = row.event_id,
                    role = row.role,
                    username = row.username,
                    displayname = row.displayname,
                    avatar_url = row.avatar_url,
                    position = idx.toLong(),
                    cached_at = now
                )
            }
            upsertTeamInternal(updated, now)
        }
        return updated
    }

    override suspend fun autoGenerate(
        eventId: String, teamCount: Int?, teamSize: Int?, includeReserves: Boolean
    ): List<TeamDomain> {
        requireOnline()
        val request = AutoGenerateTeamsRequest(
            teamSize = teamSize,
            teamCount = teamCount,
            includeReserves = includeReserves
        )
        val teams = api.autoGenerate(eventId, request).map { it.toDomain() }
        replaceTeamsForEvent(eventId, teams)
        return teams
    }

    // ── helpers de cache ───────────────────────────────────────────────────

    private fun cachedTeamsByEvent(eventId: String): List<TeamDomain> {
        val teamRows = teamQueries.selectByEvent(eventId).executeAsList()
        return teamRows.map { teamRow ->
            val playerRows = playerQueries.selectByTeam(teamRow.id).executeAsList()
            teamRow.toDomain(players = playerRows.map { it.toDomain() })
        }
    }

    /**
     * Apaga todos os times+jogadores do evento e reinsere na ordem recebida.
     * Usado tanto em `listTeams` quanto em `autoGenerate` — ambos entregam
     * o conjunto completo de times do evento.
     */
    private fun replaceTeamsForEvent(eventId: String, teams: List<TeamDomain>) {
        val now = nowMillis()
        db.transaction {
            playerQueries.clearByEvent(eventId)
            teamQueries.clearByEvent(eventId)
            teams.forEach { upsertTeamInternal(it, now) }
        }
    }

    /**
     * Upsert de 1 time + seus jogadores. Limpa os jogadores antigos do time
     * antes de inserir os novos — a resposta da API sempre é autoritativa.
     */
    private fun upsertTeam(team: TeamDomain) {
        db.transaction {
            upsertTeamInternal(team, nowMillis())
        }
    }

    private fun upsertTeamInternal(team: TeamDomain, now: Long) {
        teamQueries.upsert(
            id = team.id,
            event_id = team.eventId,
            team_order = team.order.toLong(),
            name = team.name,
            color = team.color,
            cached_at = now
        )
        playerQueries.clearByTeam(team.id)
        team.players.forEachIndexed { idx, player ->
            insertPlayer(team, player, position = idx.toLong(), now = now)
        }
    }

    private fun insertPlayer(
        team: TeamDomain,
        player: TeamPlayerDomain,
        position: Long,
        now: Long
    ) {
        playerQueries.upsert(
            team_id = team.id,
            user_id = player.userId,
            event_id = team.eventId,
            role = player.role,
            username = player.username,
            displayname = player.displayname,
            avatar_url = player.avatarUrl,
            position = position,
            cached_at = now
        )
    }

    @OptIn(ExperimentalTime::class)
    private fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()

    /** Guarda de escrita — simétrica aos demais repositórios. */
    private fun requireOnline() {
        if (!connectivity.isOnlineNow()) throw OfflineException()
    }
}
