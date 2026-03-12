package app.loobby.feature.events.domain.model

data class CreateEventInput(
    val eventType: EventType,
    val name: String,
    val description: String?,
    val scheduledDatetime: String,  // ISO-8601
    val gameplay: CreateGameplayInput?,
    val sport: CreateSportInput?
)

data class CreateGameplayInput(
    val gameName: String,
    val gameId: String? = null
)

data class CreateSportInput(
    val durationMinutes: Int,
    val arena: String? = null,
    val pricePerPlayer: Double? = null,
    val maxPlayers: Int? = null,
    val acceptReserve: Boolean? = null
)