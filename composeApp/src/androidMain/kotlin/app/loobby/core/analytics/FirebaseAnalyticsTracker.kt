package app.loobby.core.analytics

import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent

/**
 * Envia os eventos ao Google Analytics, que os junta e manda em lotes. No debug, a
 * coleta fica desativada pelo manifesto e nada sai do aparelho.
 *
 * Nenhum id de usuário é associado ao Analytics: os eventos ficam ligados só ao id
 * de instalação que o próprio Firebase gera.
 */
class FirebaseAnalyticsTracker : AnalyticsTracker {

    override fun track(event: AnalyticsEvent) {
        Firebase.analytics.logEvent(event.name) {
            event.params.forEach { (key, value) -> param(key, value) }
        }
    }
}
