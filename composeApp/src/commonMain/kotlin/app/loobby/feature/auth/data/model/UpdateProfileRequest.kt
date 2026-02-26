package app.loobby.feature.auth.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileRequest(
    val username: String? = null,
    val displayname: String? = null,
    val avatarUrl: String? = null
)