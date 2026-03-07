package app.loobby.feature.events.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EventResponse(
    val id: String,
    val eventType: String,
    val groupId: String?,
    val isInstant: Boolean,
    val ownerId: String,
    val scheduledDatetime: String,
    val name: String,
    val description: String?,
    val inviteCode: String,
    val createdAt: String,
    val gameplay: GameplayResponse?,
    val sport: SportResponse?,
    val rsvpStatus: String?
)

@Serializable
data class GameplayResponse(
    val gameId: String?,
    val gameName: String
)

@Serializable
data class SportResponse(
    val durationMinutes: Int,
    val arena: String?,
    val pricePerPlayer: Double,
    val maxPlayers: Int?,
    val acceptReserve: Boolean
)