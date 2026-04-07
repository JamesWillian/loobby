package app.loobby.feature.groups.presentation

import app.loobby.core.preferences.UserPreferencesRepository
import app.loobby.feature.auth.domain.repository.AuthRepository
import app.loobby.feature.groups.domain.model.FeedType
import app.loobby.feature.groups.domain.model.InvitePreview
import app.loobby.feature.groups.domain.usecase.*
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
    private val authRepository: AuthRepository,
    private val listMyFeedUseCase: ListMyFeedUseCase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _uiState = MutableStateFlow(GroupsUiState())
    val uiState: StateFlow<GroupsUiState> = _uiState.asStateFlow()

    init {
        scope.launch {
            authRepository.sessionFlow
                .map { it?.userId }
                .distinctUntilChanged()
                .collect { userId ->
                    prefs.clearLastSelectedFeedItem()
                    _uiState.value = GroupsUiState()

                    if (userId != null) {
                        refreshMyFeed(userId)
                    }
                }
        }
    }

    /** Leitura síncrona do último item selecionado (para rota inicial). */
    fun getLastSelectedFeedId(): String? = prefs.getLastSelectedFeedId()
    fun getLastSelectedFeedType(): String? = prefs.getLastSelectedFeedType()

    /** Mantém compatibilidade com código existente */
    fun getLastSelectedGroupId(): String? = prefs.getLastSelectedGroupId()

    // ── Feed (sidebar unificada) ────────────────────────────────────

    fun refreshMyFeed(userId: String? = null) {
        scope.launch {
            setLoading(true)
            try {
                val uid = userId ?: authRepository.sessionFlow.first()?.userId ?: ""
                val result = listMyFeedUseCase(uid)
                // Extrai os groups do feed para manter compatibilidade com selectedGroup
                val groups = listMyGroups()
                _uiState.update {
                    it.copy(
                        feed = result,
                        groups = groups,
                        lastMessage = "Loaded ${result.size} feed items",
                        errorMessage = null
                    )
                }
                restoreLastSelectedFeedItem(result.map { it.id })
            } catch (t: Throwable) {
                setError(t)
            } finally {
                setLoading(false)
            }
        }
    }

    /** Chamado antigo — delega para refreshMyFeed */
    fun refreshMyGroups() {
        refreshMyFeed()
    }

    private fun restoreLastSelectedFeedItem(availableIds: List<String>) {
        val lastId = prefs.getLastSelectedFeedId()
        val lastType = prefs.getLastSelectedFeedType()

        if (lastId != null && lastId in availableIds) {
            val feedType = if (lastType == "EVENT") FeedType.EVENT else FeedType.GROUP
            selectFeedItem(lastId, feedType)
        } else if (availableIds.isNotEmpty()) {
            // Seleciona o primeiro item do feed
            val first = _uiState.value.feed.firstOrNull() ?: return
            selectFeedItem(first.id, first.entryType)
        }
    }

    /**
     * Seleciona um item do feed (grupo ou evento instantâneo).
     * Salva nas prefs e atualiza o state.
     */
    fun selectFeedItem(id: String, type: FeedType) {
        val typeStr = if (type == FeedType.EVENT) "EVENT" else "GROUP"
        prefs.saveLastSelectedFeedItem(id, typeStr)

        _uiState.update {
            it.copy(
                selectedFeedId = id,
                selectedFeedType = type
            )
        }

        // Se for GROUP, carrega o grupo completo para as telas internas
        if (type == FeedType.GROUP) {
            loadGroup(id)
        }
    }

    // ── Create group ────────────────────────────────────────────────

    fun createNewGroup(name: String, onSuccess: (groupId: String, groupName: String) -> Unit) {
        scope.launch {
            _uiState.update { it.copy(isCreatingGroup = true, createGroupError = null) }
            try {
                val group = createGroup(name, null)
                prefs.saveLastSelectedFeedItem(group.id, "GROUP")

                // Refresh feed para incluir o novo grupo
                val uid = authRepository.sessionFlow.first()?.userId ?: ""
                val feedResult = listMyFeedUseCase(uid)
                val groupsResult = listMyGroups()

                _uiState.update {
                    it.copy(
                        isCreatingGroup = false,
                        feed = feedResult,
                        groups = groupsResult,
                        selectedGroup = group,
                        selectedFeedId = group.id,
                        selectedFeedType = FeedType.GROUP,
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

    fun confirmJoinByInvite(onSuccess: (groupId: String, groupName: String) -> Unit) {
        val preview = _uiState.value.invitePreview ?: return

        scope.launch {
            _uiState.update { it.copy(isJoiningByInvite = true, inviteError = null) }
            try {
                when (preview) {
                    is InvitePreview.GroupPreview -> {
                        joinGroup(preview.id)
                        prefs.saveLastSelectedFeedItem(preview.id, "GROUP")

                        val uid = authRepository.sessionFlow.first()?.userId ?: ""
                        val feedResult = listMyFeedUseCase(uid)
                        val groupsResult = listMyGroups()
                        val group = getGroupById(preview.id)

                        _uiState.update {
                            it.copy(
                                isJoiningByInvite = false,
                                feed = feedResult,
                                groups = groupsResult,
                                selectedGroup = group,
                                selectedFeedId = preview.id,
                                selectedFeedType = FeedType.GROUP,
                                invitePreview = null,
                                inviteError = null,
                                lastMessage = "Entrou no grupo: ${preview.name}"
                            )
                        }

                        onSuccess(preview.id, preview.name)
                    }

                    is InvitePreview.EventPreview -> {
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
                refreshMyFeed()
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
                prefs.saveLastSelectedFeedItem(groupId, "GROUP")
                _uiState.update {
                    it.copy(
                        selectedGroup = group,
                        selectedFeedId = groupId,
                        selectedFeedType = FeedType.GROUP,
                        lastMessage = "Loaded group: ${group.name}",
                        errorMessage = null
                    )
                }
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
                prefs.clearLastSelectedFeedItem()
                _uiState.update { it.copy(lastMessage = "Left group", errorMessage = null) }
                refreshMyFeed()
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
                prefs.clearLastSelectedFeedItem()
                _uiState.update { it.copy(isDeletingGroup = false, deleteGroupSuccess = true, selectedGroup = null, selectedFeedId = null, selectedFeedType = null) }
                refreshMyFeed()
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