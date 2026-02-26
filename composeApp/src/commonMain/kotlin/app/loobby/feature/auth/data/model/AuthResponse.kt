package app.loobby.feature.auth.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val userId: String,
    val username: String? = null,
    val roles: List<String> = emptyList()
)