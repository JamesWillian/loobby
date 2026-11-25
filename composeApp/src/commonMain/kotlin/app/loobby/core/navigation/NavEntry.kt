package app.loobby.core.navigation

import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

/**
 * Uma tela aberta numa pilha. Ela é dona dos próprios ViewModels: duas gameplays
 * abertas, uma sobre a outra, não dividem estado, e o ViewModel vive até a tela
 * sair da pilha, mesmo enquanto outra aba está à frente.
 *
 * [id] é único no app inteiro e identifica também o estado salvo da tela (rolagem,
 * campos digitados).
 */
class NavEntry internal constructor(
    val id: Long,
    val route: AppRoute,
) : ViewModelStoreOwner {

    override val viewModelStore: ViewModelStore = ViewModelStore()

    internal fun dispose() = viewModelStore.clear()
}
