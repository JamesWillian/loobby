package app.loobby.feature.events.data.model

import kotlinx.serialization.Serializable

@Serializable
data class RsvpResponse(
    val eventId: String,
    val userId: String,
    val status: String,
    val isPaid: Boolean,
    val obs: String? = null,
    val createdAt: String? = null,
    val username: String,
    val displayname: String?,
    val avatarUrl: String? = null,
    val isOwner: Boolean
)