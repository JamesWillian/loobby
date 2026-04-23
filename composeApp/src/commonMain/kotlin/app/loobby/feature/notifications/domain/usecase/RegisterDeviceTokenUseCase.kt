package app.loobby.feature.notifications.domain.usecase

import app.loobby.feature.notifications.domain.repository.DeviceTokenRepository

/**
 * Use case chamado logo após login bem-sucedido. Se o token não puder ser
 * obtido (Firebase ainda não inicializou, permissão negada, etc.) ou se o
 * backend responder com erro, registra no log mas não propaga — o login
 * não deve falhar por causa de notificações.
 */
class RegisterDeviceTokenUseCase(
    private val repository: DeviceTokenRepository
) {
    suspend operator fun invoke(): Result<String?> = runCatching {
        repository.registerCurrentToken()
    }
}

class UnregisterDeviceTokenUseCase(
    private val repository: DeviceTokenRepository
) {
    suspend operator fun invoke(token: String): Result<Unit> = runCatching {
        repository.unregister(token)
    }
}
