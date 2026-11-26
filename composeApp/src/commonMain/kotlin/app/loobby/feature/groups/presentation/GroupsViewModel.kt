package app.loobby.feature.groups.presentation

import app.loobby.core.analytics.AnalyticsEvent
import app.loobby.core.analytics.AnalyticsTracker
import app.loobby.core.analytics.CrashReporter
import app.loobby.core.analytics.reportIfUnexpected
import app.loobby.core.media.ImagePrefetcher
import app.loobby.feature.auth.domain.repository.AuthRepository
import app.loobby.feature.groups.domain.usecase.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Operações de GRUPO: criar, detalhe, membros, renomear, trocar a imagem, sair e
 * excluir. Usado pelo sheet de criar grupo e pela tela de detalhes do grupo. A lista
 * de grupos da barra lateral é do HomeViewModel.
 */
class GroupsViewModel(
    private val createGroup: CreateGroupUseCase,
    private val listMyGroups: ListMyGroupsUseCase,
    private val getGroupById: GetGroupByIdUseCase,
    private val leaveGroup: LeaveGroupUseCase,
    private val listMembers: ListGroupMembersUseCase,
    private val updateGroupUseCase: UpdateGroupUseCase,
    private val uploadGroupImageUseCase: UploadGroupImageUseCase,
    private val deleteGroupUseCase: DeleteGroupUseCase,
    private val removeMemberUseCase: RemoveGroupMemberUseCase,
    private val authRepository: AuthRepository,
    private val imagePrefetcher: ImagePrefetcher,
    private val analytics: AnalyticsTracker,
    private val crashReporter: CrashReporter
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _uiState = MutableStateFlow(GroupsUiState())
    val uiState: StateFlow<GroupsUiState> = _uiState.asStateFlow()

    init {
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
                analytics.track(AnalyticsEvent.GroupCreated)

                _uiState.update {
                    it.copy(
                        isCreatingGroup = false,
                        selectedGroup = group,
                        createGroupError = null,
                        lastMessage = "Grupo criado: ${group.name}"
                    )
                }

                onSuccess(group.id, group.name)
            } catch (t: Throwable) {
                crashReporter.reportIfUnexpected(t)
                _uiState.update {
                    it.copy(
                        isCreatingGroup = false,
                        createGroupError = t.message ?: "Erro ao criar grupo"
                    )
                }
            }
        }
    }

    /** Carrega o detalhe de um grupo. */
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
                // Garante que a capa do grupo esteja no disco para exibição offline.
                imagePrefetcher.prefetch(group.imageUrl)
            } catch (t: Throwable) {
                crashReporter.reportIfUnexpected(t)
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
                analytics.track(AnalyticsEvent.GroupLeft)
                _uiState.update {
                    it.copy(
                        lastMessage = "Left group",
                        errorMessage = null,
                        selectedGroup = null
                    )
                }
            } catch (t: Throwable) {
                crashReporter.reportIfUnexpected(t)
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
                // Pré-carrega os avatares dos membros para a tela de detalhe
                // continuar funcional se o usuário ficar offline.
                imagePrefetcher.prefetch(result.map { it.avatarUrl })
            } catch (t: Throwable) {
                crashReporter.reportIfUnexpected(t)
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
                analytics.track(AnalyticsEvent.GroupRenamed)
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
                crashReporter.reportIfUnexpected(t)
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
                analytics.track(AnalyticsEvent.GroupImageUpdated)
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
                crashReporter.reportIfUnexpected(t)
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
                analytics.track(AnalyticsEvent.GroupDeleted)
                _uiState.update {
                    it.copy(
                        isDeletingGroup = false,
                        deleteGroupSuccess = true,
                        selectedGroup = null
                    )
                }
            } catch (t: Throwable) {
                crashReporter.reportIfUnexpected(t)
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
                analytics.track(AnalyticsEvent.GroupMemberRemoved)
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
                crashReporter.reportIfUnexpected(t)
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
