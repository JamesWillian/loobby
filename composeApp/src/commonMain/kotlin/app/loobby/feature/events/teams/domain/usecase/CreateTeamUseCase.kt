package app.loobby.feature.events.teams.domain.usecase

import app.loobby.feature.events.teams.domain.model.TeamDomain
import app.loobby.feature.events.teams.domain.repository.TeamsRepository

class CreateTeamUseCase(private val repository: TeamsRepository) {
    suspend operator fun invoke(
        eventId: String,
        name: String,
        color: String? = null,
        playerIds: List<String> = emptyList()
    ): TeamDomain = repository.createTeam(eventId, name, color, playerIds)
}