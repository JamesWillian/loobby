package app.loobby.feature.notifications.data.remote

import app.loobby.feature.notifications.data.model.RegisterDeviceRequest

/**
 * Espelha o DeviceTokenController do backend.
 *
 * Endpoints (autenticados):
 *   POST   /devices/register   → registra/atualiza token FCM
 *   DELETE /devices/{token}    → remove token (logout)
 */
interface DeviceTokenApi {
    suspend fun register(request: RegisterDeviceRequest)
    suspend fun unregister(token: String)
}
