package app.loobby.feature.groups.presentation

import app.loobby.feature.groups.data.model.GroupMemberResponse
import app.loobby.feature.groups.domain.model.GroupDomain

data class GroupsUiState(
    val isLoading: Boolean = false,
    val groups: List<GroupDomain> = emptyList(),
    val selectedGroup: GroupDomain? = null,
    val members: List<GroupMemberResponse> = emptyList(),
    val lastMessage: String? = null,
    val errorMessage: String? = null
)