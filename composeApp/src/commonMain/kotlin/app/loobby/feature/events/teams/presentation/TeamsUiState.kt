package app.loobby.feature.events.teams.presentation

import app.loobby.feature.events.domain.model.RsvpDomain
import app.loobby.feature.events.teams.domain.model.TeamDomain
import app.loobby.feature.events.teams.domain.model.TeamPlayerDomain

/** Cores disponíveis para times — mesma paleta do backend */
val TEAM_COLORS = listOf(
    "#F44336", // vermelho
    "#FF9800", // laranja
    "#FFC107", // amarelo
    "#4CAF50", // verde
    "#009688", // teal
    "#2196F3", // azul
    "#7C4DFF", // roxo
    "#E91E63", // rosa
    "#78909C", // cinza
    "#AEEA00"  // lima
)

data class TeamsUiState(
    val isLoading: Boolean = false,
    val teams: List<TeamDomain> = emptyList(),
    val confirmedPlayers: List<RsvpDomain> = emptyList(),
    val reservePlayers: List<RsvpDomain> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    /** Total de jogadores em todos os times */
    val totalPlayersInTeams: Int
        get() = teams.sumOf { it.players.size }

    /** Total de jogadores confirmados (status YES) */
    val totalConfirmedPlayers: Int
        get() = confirmedPlayers.size

    /** Total de jogadores reservas (status RESERVE) */
    val totalReservedPlayers: Int
        get() = reservePlayers.size

    /** Jogadores que não estão em nenhum time */
    val unassignedPlayers: List<RsvpDomain>
        get() {
            val assignedIds = teams.flatMap { t -> t.players.map { it.userId } }.toSet()
            return confirmedPlayers.filter { it.userId !in assignedIds }
        }

    /** Média de jogadores por time */
    val averagePlayersPerTeam: Double
        get() = if (teams.isEmpty()) 0.0 else totalPlayersInTeams.toDouble() / teams.size

    /** Jogadores disponíveis (confirmados que NÃO estão no time especificado) */
    fun availablePlayersForTeam(teamId: String): List<RsvpDomain> {
        val teamPlayerIds = teams.find { it.id == teamId }?.players?.map { it.userId }?.toSet() ?: emptySet()
        val allTeamPlayerIds = teams.flatMap { t -> t.players.map { it.userId } }.toSet()
        // Disponíveis = confirmados que não estão em NENHUM time, ou que já estão neste time
        return confirmedPlayers.filter { it.userId !in allTeamPlayerIds || it.userId in teamPlayerIds }
    }
}