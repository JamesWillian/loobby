package app.loobby.feature.groups.data.model

import kotlinx.serialization.Serializable

@Serializable
data class GroupMemberResponse(
    val userId: String,
    val username: String,
    val displayname: String? = null,
    val avatarUrl: String? = null,
    val isOwner: Boolean
)