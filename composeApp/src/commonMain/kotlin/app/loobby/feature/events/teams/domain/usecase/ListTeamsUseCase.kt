package app.loobby.feature.events.teams.domain.usecase

import app.loobby.feature.events.teams.domain.model.TeamDomain
import app.loobby.feature.events.teams.domain.repository.TeamsRepository

class ListTeamsUseCase(private val repository: TeamsRepository) {
    suspend operator fun invoke(eventId: String): List<TeamDomain> =
        repository.listTeams(eventId)
}