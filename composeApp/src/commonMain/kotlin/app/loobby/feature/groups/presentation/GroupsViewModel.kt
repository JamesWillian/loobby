package app.loobby.feature.groups.presentation

import app.loobby.feature.groups.domain.usecase.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GroupsViewModel(
    private val createGroup: CreateGroupUseCase,
    private val listMyGroups: ListMyGroupsUseCase,
    private val getGroupById: GetGroupByIdUseCase,
    private val joinGroup: JoinGroupUseCase,
    private val leaveGroup: LeaveGroupUseCase,
    private val listMembers: ListGroupMembersUseCase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _uiState = MutableStateFlow(GroupsUiState())
    val uiState: StateFlow<GroupsUiState> = _uiState.asStateFlow()

    fun refreshMyGroups() {
        scope.launch {
            setLoading(true)
            try {
                val result = listMyGroups()
                _uiState.update { it.copy(groups = result, lastMessage = "Loaded ${result.size} groups", errorMessage = null) }
            } catch (t: Throwable) {
                setError(t)
            } finally {
                setLoading(false)
            }
        }
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

    private fun setLoading(value: Boolean) {
        _uiState.update { it.copy(isLoading = value) }
    }

    private fun setError(t: Throwable) {
        _uiState.update { it.copy(errorMessage = t.message ?: "Unknown error", lastMessage = null) }
    }
}