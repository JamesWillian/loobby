package app.loobby.feature.gameplays.presentation.invite

import app.loobby.feature.gameplays.domain.model.GameplayInvitePreview
import app.loobby.feature.groups.domain.model.GroupDomain

/** O que o código achou. */
sealed interface InviteFound {
    data class Group(val group: GroupDomain) : InviteFound
    data class Gameplay(val preview: GameplayInvitePreview) : InviteFound
}

data class JoinInviteUiState(
    /** Só o que vem depois do "$-"; o prefixo fica fixo no campo. */
    val code: String = "",
    val found: InviteFound? = null,
    val isSearching: Boolean = false,
    val isJoining: Boolean = false,
    val errorMessage: String? = null,
) {
    val isBusy: Boolean get() = isSearching || isJoining
}

/** Entrou: a tela que abriu o convite segue daqui. */
sealed interface InviteJoined {
    data class Group(val groupId: String, val groupName: String) : InviteJoined
    data class Gameplay(val gameplayId: String) : InviteJoined
}
