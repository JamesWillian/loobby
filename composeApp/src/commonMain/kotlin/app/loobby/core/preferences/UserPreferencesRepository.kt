package app.loobby.core.preferences

import com.russhwolf.settings.Settings

class UserPreferencesRepository(private val settings: Settings) {

    // ── Last selected group (legado, mantido para compatibilidade) ──
    fun getLastSelectedGroupId(): String? =
        settings.getStringOrNull(KEY_LAST_GROUP_ID)

    fun saveLastSelectedGroupId(groupId: String) =
        settings.putString(KEY_LAST_GROUP_ID, groupId)

    fun clearLastSelectedGroupId() =
        settings.remove(KEY_LAST_GROUP_ID)

    // ── Last selected feed item (grupo ou evento instantâneo) ───────
    fun getLastSelectedFeedId(): String? =
        settings.getStringOrNull(KEY_LAST_FEED_ID)

    fun getLastSelectedFeedType(): String? =
        settings.getStringOrNull(KEY_LAST_FEED_TYPE)

    fun saveLastSelectedFeedItem(id: String, type: String) {
        settings.putString(KEY_LAST_FEED_ID, id)
        settings.putString(KEY_LAST_FEED_TYPE, type)
        // Mantém lastGroupId em sincronia quando for GROUP
        if (type == "GROUP") {
            settings.putString(KEY_LAST_GROUP_ID, id)
        }
    }

    fun clearLastSelectedFeedItem() {
        settings.remove(KEY_LAST_FEED_ID)
        settings.remove(KEY_LAST_FEED_TYPE)
        settings.remove(KEY_LAST_GROUP_ID)
    }

    companion object {
        private const val KEY_LAST_GROUP_ID = "last_selected_group_id"
        private const val KEY_LAST_FEED_ID = "last_selected_feed_id"
        private const val KEY_LAST_FEED_TYPE = "last_selected_feed_type"
    }
}