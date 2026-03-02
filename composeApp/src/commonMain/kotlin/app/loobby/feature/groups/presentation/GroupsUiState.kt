package app.loobby.feature.groups.presentation

import app.loobby.feature.groups.data.model.GroupMemberResponse
import app.loobby.feature.groups.data.model.GroupResponse

data class GroupsUiState(
    val isLoading: Boolean = false,
    val groups: List<GroupResponse> = emptyList(),
    val selectedGroup: GroupResponse? = null,
    val members: List<GroupMemberResponse> = emptyList(),
    val lastMessage: String? = null,
    val errorMessage: String? = null
)