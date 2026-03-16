package app.loobby.feature.events.domain.model

enum class RsvpStatus { YES, NO, MAYBE, RESERVE, PENDING }

data class RsvpDomain(
    val eventId: String,
    val userId: String,
    val status: RsvpStatus,
    val isPaid: Boolean,
    val obs: String?,
    val username: String,
    val displayname: String?,
    val avatarUrl: String?,
    val isOwner: Boolean
)