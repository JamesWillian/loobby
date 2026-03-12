package app.loobby.feature.auth.domain.usecase

import app.loobby.feature.auth.data.model.UserMeResponse
import app.loobby.feature.auth.domain.repository.AuthRepository

class GetProfileUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): UserMeResponse = repository.getProfile()
}