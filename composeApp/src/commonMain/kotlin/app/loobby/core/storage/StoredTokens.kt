package app.loobby.core.storage

import kotlinx.serialization.Serializable

@Serializable
data class StoredTokens(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val username: String,
    val roles: List<String>
) {
    val isAnonymous: Boolean
        get() = roles.any { it.equals("ANON", ignoreCase = true) }
}