package app.loobby.feature.home.data

import app.loobby.feature.home.domain.HomePreferences
import app.loobby.feature.home.domain.HomeSelection
import com.russhwolf.settings.Settings

/**
 * As preferências do Início nas Settings do aparelho. As chaves são novas na 2.0:
 * a seleção da sidebar da v1 (grupo ou evento) não é lida.
 */
class SettingsHomePreferences(private val settings: Settings) : HomePreferences {

    override var selection: HomeSelection
        get() = settings.getStringOrNull(KEY_SELECTION)
            ?.takeIf { it.startsWith(GROUP_PREFIX) }
            ?.let { HomeSelection.Group(it.removePrefix(GROUP_PREFIX)) }
            ?: HomeSelection.Lobby
        set(value) {
            when (value) {
                HomeSelection.Lobby -> settings.putString(KEY_SELECTION, LOBBY)
                is HomeSelection.Group -> settings.putString(KEY_SELECTION, GROUP_PREFIX + value.groupId)
            }
        }

    override var isRailOpen: Boolean
        get() = settings.getBoolean(KEY_RAIL_OPEN, defaultValue = true)
        set(value) = settings.putBoolean(KEY_RAIL_OPEN, value)

    private companion object {
        const val KEY_SELECTION = "home_selection"
        const val KEY_RAIL_OPEN = "home_rail_open"
        const val LOBBY = "lobby"
        const val GROUP_PREFIX = "group:"
    }
}
