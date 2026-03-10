package app.loobby.core.storage

import com.russhwolf.settings.Settings
import com.russhwolf.settings.contains
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json

class SettingsTokenStorage(
    private val settings: Settings = Settings()
) : TokenStorage {

    private val KEY_TOKENS = "auth_tokens"
    private val KEY_ANONYMOUS_ID = "anonymous_user_id"

    private val state = MutableStateFlow(loadFromSettings())

    private fun loadFromSettings(): StoredTokens? {

        if (!settings.contains(KEY_TOKENS)) return null

        val json = settings[KEY_TOKENS, ""]

        if (json.isEmpty()) return null

        return try {
            Json.decodeFromString<StoredTokens>(json)
        } catch (_: Throwable) {
            null
        }
    }

    override suspend fun saveTokens(tokens: StoredTokens) {
        val json = Json.encodeToString(tokens)
        settings[KEY_TOKENS] = json
        state.value = tokens
    }

    override suspend fun clearTokens() {
        settings.remove(KEY_TOKENS)
        state.value = null
    }

    override fun getTokens(): StoredTokens? = state.value

    override fun observeTokens(): Flow<StoredTokens?> = state

    override suspend fun saveAnonymousId(anonymousUserId: String) {
        settings[KEY_ANONYMOUS_ID] = anonymousUserId
    }

    override suspend fun getAnonymousId(): String? {
        if (!settings.contains(KEY_ANONYMOUS_ID)) return null
        val id: String = settings[KEY_ANONYMOUS_ID, ""]
        return id.ifEmpty { null }
    }
}