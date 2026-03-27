package app.loobby.feature.groups.presentation

import app.loobby.feature.groups.data.model.GroupMemberResponse
import app.loobby.feature.groups.domain.model.GroupDomain
import app.loobby.feature.groups.domain.model.InvitePreview

data class GroupsUiState(
    val isLoading: Boolean = true,
    val groups: List<GroupDomain> = emptyList(),
    val selectedGroup: GroupDomain? = null,
    val members: List<GroupMemberResponse> = emptyList(),
    val lastMessage: String? = null,
    val errorMessage: String? = null,

    // ── Action sheet flows ──────────────────────────────────────────
    val isCreatingGroup: Boolean = false,
    val createGroupError: String? = null,

    val isSearchingInvite: Boolean = false,
    val isJoiningByInvite: Boolean = false,
    val invitePreview: InvitePreview? = null,
    val inviteError: String? = null
)