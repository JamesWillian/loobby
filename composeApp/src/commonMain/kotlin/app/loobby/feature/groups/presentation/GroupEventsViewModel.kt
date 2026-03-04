package app.loobby.feature.groups.presentation

import app.loobby.feature.groups.domain.model.GroupEventFilter
import app.loobby.feature.groups.domain.model.GroupEventUi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GroupEventsViewModel {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _uiState = MutableStateFlow(GroupEventsUiState())
    val uiState: StateFlow<GroupEventsUiState> = _uiState.asStateFlow()

    private val mockAvatars = listOf(
        "https://i.pravatar.cc/150?img=1",
        "https://i.pravatar.cc/150?img=2",
        "https://i.pravatar.cc/150?img=3",
        "https://i.pravatar.cc/150?img=4",
        "https://i.pravatar.cc/150?img=5",
    )

    private val mockEvents = listOf(
        GroupEventUi(
            id = "1",
            name = "Partida na praia",
            dateLabel = "Hoje, 17:00",
            participantAvatars = mockAvatars,
            extraParticipants = 6,
            isConfirmed = true,
            filter = GroupEventFilter.TODAY
        ),
        GroupEventUi(
            id = "2",
            name = "Vôlei na quadra",
            dateLabel = "Hoje, 19:30",
            participantAvatars = mockAvatars,
            extraParticipants = 8,
            isConfirmed = true,
            filter = GroupEventFilter.TODAY
        ),
        GroupEventUi(
            id = "3",
            name = "Treino no ginásio",
            dateLabel = "Hoje, 21:00",
            participantAvatars = mockAvatars,
            extraParticipants = 12,
            isConfirmed = true,
            filter = GroupEventFilter.TODAY
        ),
        GroupEventUi(
            id = "4",
            name = "Jogo no parque",
            dateLabel = "Amanhã, 15:30",
            participantAvatars = mockAvatars,
            extraParticipants = 20,
            isConfirmed = false,
            filter = GroupEventFilter.UPCOMING
        ),
        GroupEventUi(
            id = "5",
            name = "Torneio de Vôlei",
            dateLabel = "Dom, 10:00",
            participantAvatars = mockAvatars,
            extraParticipants = 25,
            isConfirmed = false,
            filter = GroupEventFilter.UPCOMING
        ),
        GroupEventUi(
            id = "6",
            name = "Copa de verão",
            dateLabel = "Sáb, 09:00",
            participantAvatars = mockAvatars.take(3),
            extraParticipants = 4,
            isConfirmed = true,
            filter = GroupEventFilter.CONFIRMED
        ),
        GroupEventUi(
            id = "7",
            name = "Pelada do mês",
            dateLabel = "15/05, 18:00",
            participantAvatars = mockAvatars.take(2),
            extraParticipants = 0,
            isConfirmed = true,
            filter = GroupEventFilter.FINISHED
        ),
    )

    fun loadForGroup(groupId: String, groupName: String) {
        _uiState.update {
            it.copy(
                groupName = groupName,
                events = mockEvents,
                isLoading = false,
                errorMessage = null
            )
        }
    }

    fun setFilter(filter: GroupEventFilter) {
        _uiState.update { it.copy(activeFilter = filter) }
    }
}