package app.loobby.core.appconfig

import com.russhwolf.settings.Settings

/** O que o /app/config deixa guardado no aparelho. */
interface AppConfigStore {
    /**
     * A última resposta lida com sucesso, crua. É ela que vale ao abrir o app, antes de a
     * rede responder, e sem conexão: uma versão bloqueada continua bloqueada offline.
     */
    fun lastConfig(): String?

    fun saveConfig(raw: String)

    /** Os avisos que a pessoa já fechou neste aparelho. */
    fun seenNoticeIds(): Set<String>

    fun markNoticeSeen(id: String)
}

class SettingsAppConfigStore(private val settings: Settings) : AppConfigStore {

    override fun lastConfig(): String? = settings.getStringOrNull(KEY_LAST_CONFIG)

    override fun saveConfig(raw: String) = settings.putString(KEY_LAST_CONFIG, raw)

    override fun seenNoticeIds(): Set<String> =
        settings.getStringOrNull(KEY_SEEN_NOTICES)
            ?.split(SEPARATOR)
            ?.filter { it.isNotEmpty() }
            ?.toSet()
            .orEmpty()

    override fun markNoticeSeen(id: String) {
        // Os mais antigos saem primeiro: a lista não cresce para sempre.
        val ids = (seenNoticeIds() + id).toList().takeLast(MAX_SEEN_NOTICES)
        settings.putString(KEY_SEEN_NOTICES, ids.joinToString(SEPARATOR))
    }

    private companion object {
        const val KEY_LAST_CONFIG = "app_config_last_response"
        const val KEY_SEEN_NOTICES = "app_config_seen_notices"
        const val SEPARATOR = "\n"
        const val MAX_SEEN_NOTICES = 20
    }
}
