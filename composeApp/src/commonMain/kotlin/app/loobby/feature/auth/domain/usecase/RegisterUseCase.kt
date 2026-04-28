package app.loobby.feature.auth.domain.usecase

import app.loobby.feature.auth.data.model.AuthResponse
import app.loobby.feature.auth.domain.model.AuthSession
import app.loobby.feature.auth.domain.repository.AuthRepository

class RegisterUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): AuthResponse {
        // O usuário anônimo se transforma em registrado: não mantemos o token
        // anônimo anterior, pois aquela "identidade" passa a ser a conta definitiva.
        val response = repository.register(email, password)
        repository.clearSavedAnonymousToken()
        return response
    }
}
