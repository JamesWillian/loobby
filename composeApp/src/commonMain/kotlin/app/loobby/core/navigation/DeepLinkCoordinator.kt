package app.loobby.core.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Uma gameplay a abrir porque a pessoa tocou num push. [groupId] vem nas do grupo;
 * na única é null. [pushType] é o `type` do push (GAMEPLAY_REMINDER, ...).
 */
data class PendingGameplayDeepLink(
    val gameplayId: String,
    val groupId: String? = null,
    val pushType: String? = null,
)

/**
 * Leva o toque num push da plataforma (Intent no Android) até a navegação comum.
 * A plataforma chama [openGameplay]; o AppShell observa [pending], navega e chama
 * [consume] para não abrir de novo.
 */
class DeepLinkCoordinator {

    private val _pending = MutableStateFlow<PendingGameplayDeepLink?>(null)
    val pending: StateFlow<PendingGameplayDeepLink?> = _pending.asStateFlow()

    fun openGameplay(gameplayId: String, groupId: String? = null, pushType: String? = null) {
        if (gameplayId.isBlank()) return
        _pending.value = PendingGameplayDeepLink(
            gameplayId = gameplayId,
            groupId = groupId?.takeIf { it.isNotBlank() },
            pushType = pushType?.takeIf { it.isNotBlank() },
        )
    }

    fun consume() {
        _pending.value = null
    }
}
