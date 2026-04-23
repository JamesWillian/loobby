package app.loobby.feature.notifications.domain.repository

import app.loobby.feature.notifications.data.model.RegisterDeviceRequest
import app.loobby.feature.notifications.data.remote.DeviceTokenApi
import app.loobby.feature.notifications.platform.PushPlatform
import app.loobby.feature.notifications.platform.PushTokenProvider

interface DeviceTokenRepository {

    /**
     * Registra o token FCM atual do device no backend, se houver token disponível.
     * Retorna o token registrado ou null se não foi possível obter.
     */
    suspend fun registerCurrentToken(): String?

    /** Remove um token conhecido no backend (logout). */
    suspend fun unregister(token: String)
}

class DeviceTokenRepositoryImpl(
    private val api: DeviceTokenApi,
    private val tokenProvider: PushTokenProvider
) : DeviceTokenRepository {

    override suspend fun registerCurrentToken(): String? {
        val token = tokenProvider.getToken() ?: return null
        api.register(
            RegisterDeviceRequest(
                token = token,
                platform = when (tokenProvider.platform) {
                    PushPlatform.ANDROID -> "ANDROID"
                    PushPlatform.IOS -> "IOS"
                }
            )
        )
        return token
    }

    override suspend fun unregister(token: String) {
        api.unregister(token)
    }
}
