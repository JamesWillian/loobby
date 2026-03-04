package app.loobby.feature.groups.domain.model

data class GroupEventUi(
    val id: String,
    val name: String,
    val dateLabel: String,
    val participantAvatars: List<String>,
    val extraParticipants: Int,
    val isConfirmed: Boolean,
    val filter: GroupEventFilter
)