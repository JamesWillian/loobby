package app.loobby.feature.events.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CreateEventRequest(
    val eventType: String,           // "SPORT" or "GAMEPLAY"
    val name: String,
    val description: String? = null,
    val scheduledDatetime: String,   // ISO-8601 e.g. "2026-03-10T20:00:00Z"
    val gameplay: CreateGameplayDetailsRequest? = null,
    val sport: CreateSportDetailsRequest? = null
)

@Serializable
data class CreateGameplayDetailsRequest(
    val gameName: String,
    val gameId: String? = null
)

@Serializable
data class CreateSportDetailsRequest(
    val durationMinutes: Int,
    val arena: String? = null,
    val pricePerPlayer: Double? = null,
    val maxPlayers: Int? = null,
    val acceptReserve: Boolean? = null
)