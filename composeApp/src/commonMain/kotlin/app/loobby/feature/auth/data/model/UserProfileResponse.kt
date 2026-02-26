package app.loobby.feature.auth.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProfileResponse(
    val id: String,
    val username: String,
    val displayname: String? = null,
    val avatarUrl: String? = null,
    val createdAt: String
)