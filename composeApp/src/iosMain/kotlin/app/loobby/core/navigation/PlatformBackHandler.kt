package app.loobby.core.navigation

import androidx.compose.runtime.Composable

// No iOS o gesto de swipe-back é nativo do sistema, então não precisamos
// interceptar nada aqui. A navegação interna é feita pelos botões "voltar"
// da própria tela.
@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // no-op
}
