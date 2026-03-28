package app.loobby.feature.events.presentation

import app.loobby.feature.events.domain.model.EventDomain
import app.loobby.feature.events.domain.model.RsvpDomain
import app.loobby.feature.events.domain.model.RsvpStatus

data class EventDetailUiState(
    val isLoading: Boolean = false,
    val event: EventDomain? = null,
    val rsvps: List<RsvpDomain> = emptyList(),
    val isRsvpLoading: Boolean = false,
    val errorMessage: String? = null,
    val isPaid: Boolean = false,
    val obs: String = "",
    val isObsSaved: Boolean = false,

    // CHANGED: campos para gerenciar edit/delete
    val currentUserId: String? = null,        // userId do usuário logado
    val isGroupOwner: Boolean = false,         // true se o usuário é dono do grupo
    val isDeleting: Boolean = false,           // loading durante exclusão
    val deleteSuccess: Boolean = false,        // sinaliza que o evento foi excluído
    val showEditSheet: Boolean = false,        // controla exibição do sheet de edição
    val updateSuccess: Boolean = false         // sinaliza que o evento foi atualizado
) {
    val rsvpsByStatus: Map<RsvpStatus, List<RsvpDomain>>
        get() = rsvps.groupBy { it.status }

    // CHANGED: true se o usuário pode editar/excluir o evento
    val canManage: Boolean
        get() {
            val uid = currentUserId ?: return false
            val ev = event ?: return false
            return ev.ownerId == uid || isGroupOwner
        }
}