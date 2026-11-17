package app.loobby.core.analytics

/**
 * Registra o uso do app. A implementação decide para onde os eventos vão: no Android,
 * o Google Analytics; no iOS, por enquanto, lugar nenhum.
 */
fun interface AnalyticsTracker {
    fun track(event: AnalyticsEvent)
}
