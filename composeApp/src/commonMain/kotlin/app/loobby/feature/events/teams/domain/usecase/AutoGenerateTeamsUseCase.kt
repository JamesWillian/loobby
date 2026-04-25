package app.loobby.feature.events.teams.domain.usecase

import app.loobby.feature.events.teams.domain.model.TeamDomain
import app.loobby.feature.events.teams.domain.repository.TeamsRepository

class AutoGenerateTeamsUseCase(private val repository: TeamsRepository) {
    suspend operator fun invoke(
        eventId: String,
        teamCount: Int? = null,
        teamSize: Int? = null,
        includeReserves: Boolean = true
    ): List<TeamDomain> = repository.autoGenerate(eventId, teamCount, teamSize, includeReserves)
}