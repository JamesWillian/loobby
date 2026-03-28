package app.loobby.feature.events.domain.model

data class UpdateEventInput(
    val name: String? = null,
    val description: String? = null,
    val scheduledDatetime: String? = null,   // ISO-8601 UTC
    val gameplay: CreateGameplayInput? = null,
    val sport: CreateSportInput? = null,
    val clearDescription: Boolean = false
)