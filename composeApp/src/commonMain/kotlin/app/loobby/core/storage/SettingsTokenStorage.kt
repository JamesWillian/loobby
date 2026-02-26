package app.loobby.core.storage

import com.russhwolf.settings.Settings
import com.russhwolf.settings.contains
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SettingsTokenStorage(
    private val settings: Settings = Settings()
) : TokenStorage {

    private val KEY_TOKENS = "auth_tokens"

    private val state = MutableStateFlow<AuthTokens?>(loadFromSettings())

    private fun loadFromSettings(): AuthTokens? {

        if (!settings.contains(KEY_TOKENS)) return null

        val json = settings[KEY_TOKENS, ""]

        if (json.isEmpty()) return null

        return try {
            Json.decodeFromString<AuthTokens>(json)
        } catch (_: Throwable) {
            null
        }
    }

    override suspend fun saveTokens(tokens: AuthTokens) {
        val json = Json.encodeToString(tokens)
        settings[KEY_TOKENS] = json
        state.value = tokens
    }

    override suspend fun clearTokens() {
        settings.remove(KEY_TOKENS)
        state.value = null
    }

    override fun getTokens(): AuthTokens? = state.value

    override fun observeTokens(): Flow<AuthTokens?> = state
}