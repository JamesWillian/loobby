package app.loobby.feature.home.presentation.shell

/** O sheet aberto por cima do app inteiro; null = nenhum. */
sealed interface ShellSheet {
    /** O "+" da barra lateral. */
    data object CreateOrJoin : ShellSheet
    data object CreateGroup : ShellSheet
    data object JoinInvite : ShellSheet

    /** Gameplay nova; com [groupId], já no grupo. */
    data class NewGameplay(val groupId: String? = null, val groupName: String? = null) : ShellSheet

    /** Login e cadastro. [welcomeName]: o apelido do anônimo, para saudar. */
    data class Auth(val welcomeName: String? = null) : ShellSheet
    data object Nickname : ShellSheet
}
