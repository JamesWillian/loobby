package app.loobby.feature.auth.data.model

import kotlinx.serialization.Serializable

/**
 * Request para PATCH /users/me
 * Todos os campos são opcionais — envia apenas o que quer atualizar.
 */
@Serializable
data class UpdateUserProfileRequest(
    val username: String? = null,
    val displayname: String? = null,
    val avatarUrl: String? = null
)