package app.loobby.feature.events.teams.domain.usecase

import app.loobby.feature.events.teams.domain.model.TeamDomain
import app.loobby.feature.events.teams.domain.repository.TeamsRepository

class AddPlayerToTeamUseCase(private val repository: TeamsRepository) {
    suspend operator fun invoke(eventId: String, teamId: String, userId: String): TeamDomain =
        repository.addPlayer(eventId, teamId, userId)
}