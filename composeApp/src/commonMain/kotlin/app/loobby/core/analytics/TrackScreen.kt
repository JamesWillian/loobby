package app.loobby.core.analytics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import org.koin.compose.koinInject

/**
 * Conta a tela [screen] no Analytics, uma vez por [key]. Girar o aparelho recria a
 * Activity com a mesma tela aberta: a chave guardada evita contar de novo.
 *
 * A [key] distingue duas aberturas da mesma tela (um grupo e depois outro) e fica só
 * no aparelho: o que vai para o Analytics é o nome fixo da tela.
 */
@Composable
fun TrackScreen(
    screen: AppScreen,
    key: String = screen.id,
    analytics: AnalyticsTracker = koinInject()
) {
    var counted by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(key) {
        if (key != counted) {
            counted = key
            analytics.track(AnalyticsEvent.ScreenView(screen))
        }
    }
}
