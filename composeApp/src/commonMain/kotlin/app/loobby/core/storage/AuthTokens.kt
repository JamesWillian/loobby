package app.loobby.core.storage

import kotlinx.serialization.Serializable

@Serializable
data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val username: String? = null,
    val roles: List<String> = emptyList(),
    val isAnonymous: Boolean = true
)