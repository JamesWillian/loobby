package app.loobby.feature.events.data.model

import kotlinx.serialization.Serializable

@Serializable
data class RsvpRequest(
    val status: String,  // YES, NO, MAYBE, RESERVE
    val isPaid: Boolean = false,
    val obs: String? = null
)

@Serializable
data class RsvpResponse(
    val eventId: String,
    val userId: String,
    val status: String,
    val isPaid: Boolean,
    val obs: String?,
    val createdAt: String?,
    val username: String,
    val displayname: String,
    val avatarUrl: String?,
    val isOwner: Boolean
)