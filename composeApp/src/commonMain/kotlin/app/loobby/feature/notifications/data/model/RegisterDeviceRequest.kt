package app.loobby.feature.notifications.data.model

import kotlinx.serialization.Serializable

/**
 * Espelha `app.loobby.notifications.dto.RegisterDeviceRequest` no backend.
 * `platform` é enviado como string (o backend mapeia para o enum DevicePlatform).
 */
@Serializable
data class RegisterDeviceRequest(
    val token: String,
    val platform: String
)
