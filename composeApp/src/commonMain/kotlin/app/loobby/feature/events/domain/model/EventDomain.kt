package app.loobby.feature.events.domain.model

enum class EventType { SPORT, GAMEPLAY }

enum class RsvpStatus { YES, NO, MAYBE, RESERVE }

data class EventDomain(
    val id: String,
    val eventType: EventType,
    val groupId: String?,
    val isInstant: Boolean,
    val ownerId: String,
    val scheduledDatetime: String,   // ISO-8601; format on UI layer
    val name: String,
    val description: String?,
    val inviteCode: String,
    val createdAt: String,
    val gameplay: GameplayDomain?,
    val sport: SportDomain?,
    val rsvpStatus: RsvpStatus?
)

data class GameplayDomain(
    val gameId: String?,
    val gameName: String
)

data class SportDomain(
    val durationMinutes: Int,
    val arena: String?,
    val pricePerPlayer: Double,
    val maxPlayers: Int?,
    val acceptReserve: Boolean
)

data class RsvpDomain(
    val eventId: String,
    val userId: String,
    val status: RsvpStatus,
    val isPaid: Boolean,
    val obs: String?,
    val username: String,
    val displayname: String,
    val avatarUrl: String?,
    val isOwner: Boolean
)