package app.loobby.feature.auth.domain.model

data class AuthSession(
    val userId: String,
    val username: String?,
    val roles: List<String>,
    val isAnonymous: Boolean
)