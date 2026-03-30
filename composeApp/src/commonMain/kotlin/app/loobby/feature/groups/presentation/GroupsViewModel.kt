package app.loobby.feature.groups.presentation

import app.loobby.core.preferences.UserPreferencesRepository
import app.loobby.feature.auth.domain.repository.AuthRepository
import app.loobby.feature.groups.domain.usecase.*
import app.loobby.feature.groups.domain.model.InvitePreview
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GroupsViewModel(
    private val createGroup: CreateGroupUseCase,
    private val listMyGroups: ListMyGroupsUseCase,
    private val getGroupById: GetGroupByIdUseCase,
    private val joinGroup: JoinGroupUseCase,
    private val leaveGroup: LeaveGroupUseCase,
    private val listMembers: ListGroupMembersUseCase,
    private val getGroupByInvite: GetGroupByInviteUseCase,
    private val prefs: UserPreferencesRepository,
    private val updateGroupUseCase: UpdateGroupUseCase,
    private val uploadGroupImageUseCase: UploadGroupImageUseCase,
    private val deleteGroupUseCase: DeleteGroupUseCase,
    private val removeMemberUseCase: RemoveGroupMemberUseCase,
    private val authRepository: AuthRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _uiState = MutableStateFlow(GroupsUiState())
    val uiState: StateFlow<GroupsUiState> = _uiState.asStateFlow()

    init {
        refreshMyGroups()
        scope.launch {
            val userId = authRepository.currentUserId()
            _uiState.update { it.copy(currentUserId = userId) }
        }
    }

    /** Leitura síncrona do último grupo selecionado (para rota inicial). */
    fun getLastSelectedGroupId(): String? = prefs.getLastSelectedGroupId()

    fun refreshMyGroups() {
        scope.launch {
            setLoading(true)
            try {
                val result = listMyGroups()
                _uiState.update {
                    it.copy(
                        groups = result,
                        lastMessage = "Loaded ${result.size} groups",
                        errorMessage = null
                    )
                }
                restoreLastSelectedGroup(result.map { it.id })
            } catch (t: Throwable) {
                setError(t)
            } finally {
                setLoading(false)
            }
        }
    }

    private fun restoreLastSelectedGroup(availableIds: List<String>) {
        val lastId = prefs.getLastSelectedGroupId() ?: return
        if (lastId in availableIds) {
            loadGroup(lastId)
        }
    }

    // ── Create group ────────────────────────────────────────────────

    /**
     * Creates a new group and auto-selects it.
     * [onSuccess] is called with the new groupId + groupName so the caller
     * can navigate / close sheets.
     */
    fun createNewGroup(name: String, onSuccess: (groupId: String, groupName: String) -> Unit) {
        scope.launch {
            _uiState.update { it.copy(isCreatingGroup = true, createGroupError = null) }
            try {
                val group = createGroup(name, null)

                // Refresh the group list so it appears in the sidebar
                val updatedList = listMyGroups()
                prefs.saveLastSelectedGroupId(group.id)

                _uiState.update {
                    it.copy(
                        isCreatingGroup = false,
                        groups = updatedList,
                        selectedGroup = group,
                        createGroupError = null,
                        lastMessage = "Grupo criado: ${group.name}"
                    )
                }

                onSuccess(group.id, group.name)
            } catch (t: Throwable) {
                _uiState.update {
                    it.copy(
                        isCreatingGroup = false,
                        createGroupError = t.message ?: "Erro ao criar grupo"
                    )
                }
            }
        }
    }

    // ── Invite code search ──────────────────────────────────────────

    fun searchInviteCode(code: String) {
        scope.launch {
            _uiState.update { it.copy(isSearchingInvite = true, inviteError = null, invitePreview = null) }
            try {
                val cleanCode = code.trim()

                // 8 chars → group invite (L-XXXXXX)
                // 11 chars → event invite (L-XXXXXXXXX)
                when {
                    cleanCode.length <= 8 -> {
                        val group = getGroupByInvite(cleanCode)
                        _uiState.update {
                            it.copy(
                                isSearchingInvite = false,
                                invitePreview = InvitePreview.GroupPreview(
                                    id = group.id,
                                    name = group.name,
                                    imageUrl = group.imageUrl
                                )
                            )
                        }
                    }
                    else -> {
                        // TODO: call getEventByInvite when route is available
                        _uiState.update {
                            it.copy(
                                isSearchingInvite = false,
                                inviteError = "Busca por evento ainda não disponível"
                            )
                        }
                    }
                }
            } catch (t: Throwable) {
                _uiState.update {
                    it.copy(
                        isSearchingInvite = false,
                        inviteError = t.message ?: "Código não encontrado"
                    )
                }
            }
        }
    }

    /**
     * Confirms joining the group/event found via invite code.
     * [onSuccess] is called with the groupId + name so the caller can navigate.
     */
    fun confirmJoinByInvite(onSuccess: (groupId: String, groupName: String) -> Unit) {
        val preview = _uiState.value.invitePreview ?: return

        scope.launch {
            _uiState.update { it.copy(isJoiningByInvite = true, inviteError = null) }
            try {
                when (preview) {
                    is InvitePreview.GroupPreview -> {
                        joinGroup(preview.id)

                        val updatedList = listMyGroups()
                        prefs.saveLastSelectedGroupId(preview.id)

                        val group = getGroupById(preview.id)

                        _uiState.update {
                            it.copy(
                                isJoiningByInvite = false,
                                groups = updatedList,
                                selectedGroup = group,
                                invitePreview = null,
                                inviteError = null,
                                lastMessage = "Entrou no grupo: ${preview.name}"
                            )
                        }

                        onSuccess(preview.id, preview.name)
                    }

                    is InvitePreview.EventPreview -> {
                        // TODO: join event when route is available
                        _uiState.update {
                            it.copy(
                                isJoiningByInvite = false,
                                inviteError = "Entrar em evento ainda não disponível"
                            )
                        }
                    }
                }
            } catch (t: Throwable) {
                _uiState.update {
                    it.copy(
                        isJoiningByInvite = false,
                        inviteError = t.message ?: "Erro ao entrar"
                    )
                }
            }
        }
    }

    fun clearInvitePreview() {
        _uiState.update { it.copy(invitePreview = null, inviteError = null) }
    }


    fun create(name: String, imageUrl: String?) {
        scope.launch {
            setLoading(true)
            try {
                val group = createGroup(name, imageUrl)
                _uiState.update { it.copy(lastMessage = "Created: ${group.name}", errorMessage = null) }
                refreshMyGroups()
            } catch (t: Throwable) {
                setError(t)
            } finally {
                setLoading(false)
            }
        }
    }

    fun loadGroup(groupId: String) {
        scope.launch {
            setLoading(true)
            try {
                val group = getGroupById(groupId)
                prefs.saveLastSelectedGroupId(groupId)
                _uiState.update { it.copy(selectedGroup = group, lastMessage = "Loaded group: ${group.name}", errorMessage = null) }
            } catch (t: Throwable) {
                setError(t)
            } finally {
                setLoading(false)
            }
        }
    }

    fun join(groupId: String) {
        scope.launch {
            setLoading(true)
            try {
                joinGroup(groupId)
                _uiState.update { it.copy(lastMessage = "Joined group", errorMessage = null) }
            } catch (t: Throwable) {
                setError(t)
            } finally {
                setLoading(false)
            }
        }
    }

    fun leave(groupId: String) {
        scope.launch {
            setLoading(true)
            try {
                leaveGroup(groupId)
                prefs.clearLastSelectedGroupId()
                _uiState.update { it.copy(lastMessage = "Left group", errorMessage = null) }
                refreshMyGroups()
            } catch (t: Throwable) {
                setError(t)
            } finally {
                setLoading(false)
            }
        }
    }

    fun loadMembers(groupId: String) {
        scope.launch {
            setLoading(true)
            try {
                val result = listMembers(groupId)
                _uiState.update { it.copy(members = result, lastMessage = "Loaded ${result.size} members", errorMessage = null) }
            } catch (t: Throwable) {
                setError(t)
            } finally {
                setLoading(false)
            }
        }
    }

    fun updateGroupName(groupId: String, newName: String) {
        scope.launch {
            _uiState.update { it.copy(isUpdatingGroup = true, errorMessage = null) }
            try {
                val updated = updateGroupUseCase(groupId, newName)
                val updatedList = listMyGroups()
                _uiState.update {
                    it.copy(
                        isUpdatingGroup = false,
                        selectedGroup = updated,
                        groups = updatedList,
                        groupActionMessage = "Nome atualizado"
                    )
                }
                clearActionMessage()
            } catch (t: Throwable) {
                _uiState.update {
                    it.copy(isUpdatingGroup = false, errorMessage = t.message ?: "Erro ao renomear grupo")
                }
            }
        }
    }

    fun uploadGroupImage(groupId: String, imageBytes: ByteArray, fileName: String) {
        scope.launch {
            _uiState.update { it.copy(isUpdatingGroup = true, errorMessage = null) }
            try {
                val updated = uploadGroupImageUseCase(groupId, imageBytes, fileName)
                val updatedList = listMyGroups()
                _uiState.update {
                    it.copy(
                        isUpdatingGroup = false,
                        selectedGroup = updated,
                        groups = updatedList,
                        groupActionMessage = "Imagem atualizada"
                    )
                }
                clearActionMessage()
            } catch (t: Throwable) {
                _uiState.update {
                    it.copy(isUpdatingGroup = false, errorMessage = t.message ?: "Erro ao enviar imagem")
                }
            }
        }
    }

    fun deleteGroup(groupId: String) {
        scope.launch {
            _uiState.update { it.copy(isDeletingGroup = true, errorMessage = null) }
            try {
                deleteGroupUseCase(groupId)
                prefs.clearLastSelectedGroupId()
                _uiState.update { it.copy(isDeletingGroup = false, deleteGroupSuccess = true, selectedGroup = null) }
                refreshMyGroups()
            } catch (t: Throwable) {
                _uiState.update {
                    it.copy(isDeletingGroup = false, errorMessage = t.message ?: "Erro ao excluir grupo")
                }
            }
        }
    }

    fun removeMember(groupId: String, memberId: String) {
        scope.launch {
            _uiState.update { it.copy(isRemovingMember = true, errorMessage = null) }
            try {
                removeMemberUseCase(groupId, memberId)
                // Recarrega lista de membros
                val result = listMembers(groupId)
                _uiState.update {
                    it.copy(
                        isRemovingMember = false,
                        members = result,
                        groupActionMessage = "Membro removido"
                    )
                }
                clearActionMessage()
            } catch (t: Throwable) {
                _uiState.update {
                    it.copy(isRemovingMember = false, errorMessage = t.message ?: "Erro ao remover membro")
                }
            }
        }
    }

    fun clearDeleteGroupSuccess() {
        _uiState.update { it.copy(deleteGroupSuccess = false) }
    }

    private fun clearActionMessage() {
        scope.launch {
            delay(3_000)
            _uiState.update { it.copy(groupActionMessage = null) }
        }
    }

    private fun setLoading(value: Boolean) {
        _uiState.update { it.copy(isLoading = value) }
    }

    private fun setError(t: Throwable) {
        _uiState.update { it.copy(errorMessage = t.message ?: "Unknown error", lastMessage = null) }
    }
}