package app.loobby.core.preferences

import app.loobby.feature.events.domain.model.RsvpStatus
import com.russhwolf.settings.Settings

/**
 * Persistência das preferências escolhidas pelo usuário no diálogo de
 * compartilhamento de evento. Tudo é armazenado como tipos primitivos no
 * `Settings` (compartilhado entre Android e iOS via multiplatform-settings).
 *
 * Os defaults aplicam-se quando a chave ainda não existe no Settings — por
 * isso usamos `getStringOrNull`/`hasKey` em vez dos `getBoolean(..., default)`
 * em alguns casos: queremos diferenciar "nunca configurado" (usar default)
 * de "configurado para `false`" (respeitar a escolha).
 */
class SharePreferencesRepository(private val settings: Settings) {

    // ── Identificação dos campos: emoji vs texto ──────────────────────────────
    fun getUseEmoji(): Boolean =
        settings.getBoolean(KEY_USE_EMOJI, DEFAULT_USE_EMOJI)

    fun setUseEmoji(value: Boolean) =
        settings.putBoolean(KEY_USE_EMOJI, value)

    // ── Inclusão da lista de presença ────────────────────────────────────────
    fun getIncludeList(): Boolean =
        settings.getBoolean(KEY_INCLUDE_LIST, DEFAULT_INCLUDE_LIST)

    fun setIncludeList(value: Boolean) =
        settings.putBoolean(KEY_INCLUDE_LIST, value)

    // ── Confirmação de pagamento ─────────────────────────────────────────────
    fun getIncludePayment(): Boolean =
        settings.getBoolean(KEY_INCLUDE_PAYMENT, DEFAULT_INCLUDE_PAYMENT)

    fun setIncludePayment(value: Boolean) =
        settings.putBoolean(KEY_INCLUDE_PAYMENT, value)

    // ── Comentários (obs) dos participantes ──────────────────────────────────
    fun getIncludeObservations(): Boolean =
        settings.getBoolean(KEY_INCLUDE_OBSERVATIONS, DEFAULT_INCLUDE_OBSERVATIONS)

    fun setIncludeObservations(value: Boolean) =
        settings.putBoolean(KEY_INCLUDE_OBSERVATIONS, value)

    // ── Status de RSVP selecionados (chip-list) ──────────────────────────────
    /**
     * Retorna o conjunto persistido de status. Retorna `null` quando nunca
     * foi configurado — o caller deve, nesse caso, usar todos os status
     * disponíveis como default.
     */
    fun getSelectedStatuses(): Set<RsvpStatus>? {
        val raw = settings.getStringOrNull(KEY_SELECTED_STATUSES) ?: return null
        return raw.split(',')
            .filter { it.isNotEmpty() }
            .mapNotNull { name ->
                runCatching { RsvpStatus.valueOf(name) }.getOrNull()
            }
            .toSet()
    }

    fun setSelectedStatuses(statuses: Set<RsvpStatus>) {
        settings.putString(KEY_SELECTED_STATUSES, statuses.joinToString(",") { it.name })
    }

    companion object {
        private const val KEY_USE_EMOJI           = "share_use_emoji"
        private const val KEY_INCLUDE_LIST        = "share_include_list"
        private const val KEY_INCLUDE_PAYMENT     = "share_include_payment"
        private const val KEY_INCLUDE_OBSERVATIONS = "share_include_observations"
        private const val KEY_SELECTED_STATUSES   = "share_selected_statuses"

        private const val DEFAULT_USE_EMOJI           = true
        private const val DEFAULT_INCLUDE_LIST        = true
        private const val DEFAULT_INCLUDE_PAYMENT     = false
        private const val DEFAULT_INCLUDE_OBSERVATIONS = false
    }
}
