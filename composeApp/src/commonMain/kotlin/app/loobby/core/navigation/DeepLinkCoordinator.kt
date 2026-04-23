package app.loobby.core.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Representa um deep link pra um evento vindo de uma push notification.
 * [groupId] é `null` para eventos instantâneos (sem grupo pai).
 */
data class PendingEventDeepLink(
    val eventId: String,
    val groupId: String? = null
)

/**
 * Coordena deep links entre a camada de plataforma (Android Intent / iOS userInfo)
 * e o mundo comum Compose.
 *
 * A plataforma chama [pushEventDeepLink] quando detecta que o app foi aberto a partir
 * de uma push notification com `eventId` (e opcionalmente `groupId`). O [AppShell]
 * observa [pending] e, assim que for não-nulo, navega e chama [consume] para evitar
 * navegação em loop.
 *
 * Uso:
 * - Registrado como singleton no coreModule
 * - MainActivity chama pushEventDeepLink em onCreate/onNewIntent
 * - AppShell faz collectAsState em pending
 */
class DeepLinkCoordinator {

    private val _pending = MutableStateFlow<PendingEventDeepLink?>(null)
    val pending: StateFlow<PendingEventDeepLink?> = _pending.asStateFlow()

    fun pushEventDeepLink(eventId: String, groupId: String? = null) {
        if (eventId.isBlank()) return
        _pending.value = PendingEventDeepLink(
            eventId = eventId,
            groupId = groupId?.takeIf { it.isNotBlank() }
        )
    }

    fun consume() {
        _pending.value = null
    }
}
