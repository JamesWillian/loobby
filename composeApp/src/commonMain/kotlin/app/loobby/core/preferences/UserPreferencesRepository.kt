package app.loobby.core.preferences

import com.russhwolf.settings.Settings

class UserPreferencesRepository(private val settings: Settings) {

    fun getLastSelectedGroupId(): String? =
        settings.getStringOrNull(KEY_LAST_GROUP_ID)

    fun saveLastSelectedGroupId(groupId: String) =
        settings.putString(KEY_LAST_GROUP_ID, groupId)

    fun clearLastSelectedGroupId() =
        settings.remove(KEY_LAST_GROUP_ID)

    companion object {
        private const val KEY_LAST_GROUP_ID = "last_selected_group_id"
    }
}