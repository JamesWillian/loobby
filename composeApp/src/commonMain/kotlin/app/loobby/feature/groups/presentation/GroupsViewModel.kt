package app.loobby.feature.groups.presentation

import app.loobby.feature.auth.domain.repository.AuthRepository
import app.loobby.feature.events.domain.model.RsvpStatus
import app.loobby.feature.events.domain.usecase.GetEventByInviteUseCase
import app.loobby.feature.events.domain.usecase.UpsertRsvpUseCase
import app.loobby.feature.groups.domain.model.FeedType
import app.loobby.feature.groups.domain.model.InvitePreview
import app.loobby.feature.groups.domain.usecase.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel dedicado a operações de GRUPO: CRUD, membros, convites, detalhe.
 *
 * Não gerencia mais o feed nem a seleção da sidebar — isso é responsabilidade
 * do [FeedViewModel]. Depois de criar/entrar/sair/excluir um grupo, este VM
 * delega para [feedVm] atualizar o feed e ajustar a seleção atual.
 */
class GroupsViewModel(
    private val createGroup: CreateGroupUseCase,
    private val listMyGroups: ListMyGroupsUseCase,
    private val getGroupById: GetGroupByIdUseCase,
    private val joinGroup: JoinGroupUseCase,
    private val leaveGroup: LeaveGroupUseCase,
    private val listMembers: ListGroupMembersUseCase,
    private val getGroupByInvite: GetGroupByInviteUseCase,
    private val getEventByInvite: GetEventByInviteUseCase,
    private val upsertRsvp: UpsertRsvpUseCase,
    private val updateGroupUseCase: UpdateGroupUseCase,
    private val uploadGroupImageUseCase: UploadGroupImageUseCase,
    private val deleteGroupUseCase: DeleteGroupUseCase,
    private val removeMemberUseCase: RemoveGroupMemberUseCase,
    private val authRepository: AuthRepository,
    private val feedVm: FeedViewModel
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _uiState = MutableStateFlow(GroupsUiState())
    val uiState: StateFlow<GroupsUiState> = _uiState.asStateFlow()

    init {
        // Observa mudanças de seleção no feed: quando um GRUPO for selecionado,
        // carregamos o detalhe dele; quando for EVENTO (ou nada), limpamos o
        // selectedGroup do estado para não ficar com dado obsoleto na tela.
        scope.launch {
            feedVm.uiState
                .map { it.selectedFeedId to it.selectedFeedType }
                .distinctUntilChanged()
                .collect { (id, type) ->
                    when {
                        id != null && type == FeedType.GROUP -> loadGroup(id)
                        else -> _uiState.update { it.copy(selectedGroup = null) }
                    }
                }
        }

        // Mantém o currentUserId sincronizado com a sessão autenticada.
        // Sem isso, GroupsUiState.isOwner fica sempre false e as ações de dono
        // (renomear, upload de imagem, remover membro, excluir grupo) não
        // aparecem na tela de detalhe do grupo.
        // Em logout, zera o estado de grupo deste VM.
        scope.launch {
            authRepository.sessionFlow
                .map { it?.userId }
                .distinctUntilChanged()
                .collect { userId ->
                    if (userId == null) {
                        _uiState.value = GroupsUiState(isLoading = false)
                    } else {
                        _uiState.update { it.copy(currentUserId = userId) }
                    }
                }
        }
    }

    // ── Create group ────────────────────────────────────────────────

    fun createNewGroup(name: String, onSuccess: (groupId: String, groupName: String) -> Unit) {
        scope.launch {
            _uiState.update { it.copy(isCreatingGroup = true, createGroupError = null) }
            try {
                val group = createGroup(name, null)

                _uiState.update {
                    it.copy(
                        isCreatingGroup = false,
                        selectedGroup = group,
                        createGroupError = null,
                        lastMessage = "Grupo criado: ${group.name}"
                    )
                }

                // Atualiza feed e seleciona o novo grupo como item atual.
                feedVm.refreshFeed()
                feedVm.selectFeedItem(group.id, FeedType.GROUP)

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

                // Código de grupo tem 8 caracteres ("$-" + 6 letras/números).
                // Código de evento tem 11 caracteres ("$-" + 4 + "-" + 4).
                // Usamos o tamanho para decidir qual endpoint consultar.
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
                        val event = getEventByInvite(cleanCode)
                        _uiState.update {
                            it.copy(
                                isSearchingInvite = false,
                                invitePreview = InvitePreview.EventPreview(
                                    id = event.id,
                                    name = event.name,
                                    emoji = null
                                )
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

    fun confirmJoinByInvite(
        onGroupJoined: (groupId: String, groupName: String) -> Unit,
        onEventJoined: (eventId: String, eventName: String) -> Unit = { _, _ -> }
    ) {
        val preview = _uiState.value.invitePreview ?: return

        scope.launch {
            _uiState.update { it.copy(isJoiningByInvite = true, inviteError = null) }
            try {
                when (preview) {
                    is InvitePreview.GroupPreview -> {
                        joinGroup(preview.id)
                        val group = getGroupById(preview.id)

                        _uiState.update {
                            it.copy(
                                isJoiningByInvite = false,
                                selectedGroup = group,
                                invitePreview = null,
                                inviteError = null,
                                lastMessage = "Entrou no grupo: ${preview.name}"
                            )
                        }

                        feedVm.refreshFeed()
                        feedVm.selectFeedItem(preview.id, FeedType.GROUP)

                        onGroupJoined(preview.id, preview.name)
                    }

                    is InvitePreview.EventPreview -> {
                        // Entrar num evento = confirmar RSVP como YES.
                        upsertRsvp(
                            eventId = preview.id,
                            status = RsvpStatus.YES
                        )

                        _uiState.update {
                            it.copy(
                                isJoiningByInvite = false,
                                invitePreview = null,
                                inviteError = null,
                                lastMessage = "Entrou no evento: ${preview.name}"
                            )
                        }

                        feedVm.refreshFeed()
                        feedVm.selectFeedItem(preview.id, FeedType.EVENT)

                        onEventJoined(preview.id, preview.name)
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
                feedVm.refreshFeed()
            } catch (t: Throwable) {
                setError(t)
            } finally {
                setLoading(false)
            }
        }
    }

    /**
     * Carrega o detalhe de um grupo. NÃO altera a seleção do feed
     * (quem faz isso é [FeedViewModel.selectFeedItem]).
     */
    fun loadGroup(groupId: String) {
        scope.launch {
            setLoading(true)
            try {
                val group = getGroupById(groupId)
                _uiState.update {
                    it.copy(
                        selectedGroup = group,
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
                feedVm.clearSelection()
                _uiState.update {
                    it.copy(
                        lastMessage = "Left group",
                        errorMessage = null,
                        selectedGroup = null
                    )
                }
                feedVm.refreshFeed()
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
                feedVm.refreshFeed()
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
                feedVm.refreshFeed()
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
                feedVm.clearSelection()
                _uiState.update {
                    it.copy(
                        isDeletingGroup = false,
                        deleteGroupSuccess = true,
                        selectedGroup = null
                    )
                }
                feedVm.refreshFeed()
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
