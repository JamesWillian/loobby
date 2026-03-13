package app.loobby.core.navigation

sealed class AppRoute {
    /**
     * Tela principal de um grupo: exibe eventos, filtros e ações do grupo.
     *
     * @param groupId  ID do grupo selecionado na sidebar
     * @param groupName Nome do grupo, passado para exibição imediata no header
     *                  enquanto um eventual carregamento de detalhes acontece.
     */
    data class Group(
        val groupId: String = "",
        val groupName: String = ""
    ) : AppRoute()

    data class GroupDetail(val groupId: String) : AppRoute()

}