package app.loobby.feature.groups.presentation

import app.loobby.feature.groups.data.model.GroupMemberResponse
import app.loobby.feature.groups.domain.model.GroupDomain
import app.loobby.feature.groups.domain.model.InvitePreview

/**
 * Estado específico de GRUPO (detalhe, membros, CRUD, convites).
 *
 * A lista lateral e o item atualmente selecionado vivem em [FeedUiState].
 */
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
    val inviteError: String? = null,

    val currentUserId: String? = null,
    val isDeletingGroup: Boolean = false,
    val deleteGroupSuccess: Boolean = false,
    val isUpdatingGroup: Boolean = false,
    val isRemovingMember: Boolean = false,
    val groupActionMessage: String? = null
) {
    val isOwner: Boolean
        get() {
            val uid = currentUserId ?: return false
            val group = selectedGroup ?: return false
            return group.ownerId == uid
        }
}
