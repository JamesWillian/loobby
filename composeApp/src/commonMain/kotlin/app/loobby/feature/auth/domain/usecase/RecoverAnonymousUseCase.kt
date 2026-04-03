package app.loobby.feature.auth.domain.usecase

import app.loobby.feature.auth.domain.model.AuthSession
import app.loobby.feature.auth.domain.repository.AuthRepository

class RecoverAnonymousUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(anonymousToken: String): AuthSession =
        repository.recoverAnonymous(anonymousToken)
}