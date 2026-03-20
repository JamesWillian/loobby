package app.loobby.feature.events.teams.domain.usecase

import app.loobby.feature.events.teams.domain.repository.TeamsRepository

class DeleteTeamUseCase(private val repository: TeamsRepository) {
    suspend operator fun invoke(eventId: String, teamId: String) =
        repository.deleteTeam(eventId, teamId)
}