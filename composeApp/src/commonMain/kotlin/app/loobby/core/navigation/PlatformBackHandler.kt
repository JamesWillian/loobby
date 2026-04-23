package app.loobby.core.navigation

import androidx.compose.runtime.Composable

/**
 * Intercepta o botão/gesto de voltar do sistema no Android.
 *
 * - Quando [enabled] é `true`, [onBack] é chamado no lugar do comportamento padrão.
 * - Quando [enabled] é `false`, o evento segue o fluxo padrão do sistema
 *   (ex.: fechar o app se não houver nada na back stack do sistema).
 *
 * No iOS, o gesto de swipe-back é nativo do sistema e não precisa ser interceptado,
 * então o handler é um no-op. A navegação lá continua funcionando via clique no
 * botão de voltar das telas.
 */
@Composable
expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)
