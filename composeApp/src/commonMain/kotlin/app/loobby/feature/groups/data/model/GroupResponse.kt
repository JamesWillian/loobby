package app.loobby.feature.groups.data.model

import kotlinx.serialization.Serializable

@Serializable
data class GroupResponse(
    val id: String,
    val name: String,
    val inviteCode: String,
    val imageUrl: String? = null,
    val ownerId: String,
    val createdAt: String? = null
)