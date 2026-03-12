package app.loobby.feature.groups.domain.model

/**
 * Represents the preview returned when searching by invite code.
 * Can be either a group or an event.
 *
 * Group invite codes have 8 characters (e.g., L-XXXXXX → "L-" + 6 = 8 total)
 * Event invite codes have 11 characters (e.g., E-XXXXXXXXX → "E-" + 9 = 11 total)
 */
sealed class InvitePreview {

    data class GroupPreview(
        val id: String,
        val name: String,
        val imageUrl: String?
    ) : InvitePreview()

    data class EventPreview(
        val id: String,
        val name: String,
        val emoji: String?
    ) : InvitePreview()
}