package app.loobby.feature.events.teams.domain.model

data class TeamDomain(
    val id: String,
    val eventId: String,
    val order: Int,
    val name: String,
    val color: String?,
    val players: List<TeamPlayerDomain> = emptyList()
)

data class TeamPlayerDomain(
    val userId: String,
    val role: String?,
    val username: String,
    val displayname: String?,
    val avatarUrl: String?
) {
    /** Retorna displayname se existir, senão username */
    val displayName: String get() = displayname?.takeIf { it.isNotBlank() } ?: username

    /** Iniciais para o avatar placeholder */
    val initials: String
        get() {
            val name = displayName
            val parts = name.split(" ").filter { it.isNotBlank() }
            return when {
                parts.size >= 2 -> "${parts.first().first()}${parts.last().first()}"
                parts.isNotEmpty() -> parts.first().take(2)
                else -> "?"
            }.uppercase()
        }
}