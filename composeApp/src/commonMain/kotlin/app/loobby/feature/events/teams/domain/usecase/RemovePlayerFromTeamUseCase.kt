package app.loobby.feature.events.teams.domain.usecase

import app.loobby.feature.events.teams.domain.model.TeamDomain
import app.loobby.feature.events.teams.domain.repository.TeamsRepository

class RemovePlayerFromTeamUseCase(private val repository: TeamsRepository) {
    suspend operator fun invoke(eventId: String, teamId: String, userId: String): TeamDomain =
        repository.removePlayer(eventId, teamId, userId)
}