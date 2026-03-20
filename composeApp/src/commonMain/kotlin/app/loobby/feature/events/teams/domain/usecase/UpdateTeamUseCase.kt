package app.loobby.feature.events.teams.domain.usecase

import app.loobby.feature.events.teams.domain.model.TeamDomain
import app.loobby.feature.events.teams.domain.repository.TeamsRepository

class UpdateTeamUseCase(private val repository: TeamsRepository) {
    suspend operator fun invoke(
        eventId: String,
        teamId: String,
        name: String? = null,
        color: String? = null,
        order: Int? = null
    ): TeamDomain = repository.updateTeam(eventId, teamId, name, color, order)
}