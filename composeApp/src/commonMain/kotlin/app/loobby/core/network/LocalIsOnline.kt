package app.loobby.core.network

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Estado "app está online" exposto via CompositionLocal.
 *
 * Provido uma única vez pelo [AppShell] a partir do [ConnectivityObserver].
 * Cada tela/sheet que tenha botões de escrita lê este valor para entrar em
 * estado `enabled = false` quando o aparelho está offline — evitando o
 * clique que depois lançaria OfflineException no repositório.
 *
 * `staticCompositionLocalOf` porque a maioria dos consumidores usa o valor
 * apenas para habilitar/desabilitar controles — recomposições pesadas ao
 * mudar de estado são aceitáveis e raras (toggle de rede é infrequente).
 */
val LocalIsOnline = staticCompositionLocalOf { true }
