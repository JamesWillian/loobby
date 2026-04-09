package app.loobby.feature.auth.data.model

import kotlinx.serialization.Serializable

/**
 * Resposta de GET /users/me
 * Traz todas as informações do usuário logado, incluindo status de anônimo.
 */
@Serializable
data class UserMeResponse(
    val id: String,
    val username: String,
    val displayname: String? = null,
    val avatarUrl: String? = null,
    val isAnonymous: Boolean,
    val roles: List<String>,
    val email: String? = null,
    val emailVerified: Boolean = false,
    val createdAt: String? = null
)