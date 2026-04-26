package app.loobby.core.navigation

sealed class AppRoute {
    /** Tela de boas-vindas — exibida quando o usuário não tem grupos. */
    data object Welcome : AppRoute()

    /**
     * Tela principal de um grupo: exibe eventos, filtros e ações do grupo.
     */
    data class Group(
        val groupId: String = "",
        val groupName: String = ""
    ) : AppRoute()

    data class GroupDetail(val groupId: String) : AppRoute()

    /**
     * Tela de detalhes de um evento.
     *
     * @param groupId id do grupo pai quando o evento pertence a um grupo. É usado:
     *  - pelo [AppShell] pra construir o backstack [Group → EventDetail] em deep links
     *    (garantindo a seta de voltar pro grupo)
     *  - pelo `LaunchedEffect` que reage a mudanças na sidebar, pra não "desfazer" a
     *    navegação quando o grupo selecionado no feed bate com o pai do evento aberto.
     *  `null` indica evento instantâneo (abre como root, sem seta de voltar).
     */
    data class EventDetail(
        val eventId: String,
        val eventName: String,
        val groupId: String? = null
    ) : AppRoute()

    data class Teams(
        val eventId: String,
        val eventName: String
    ) : AppRoute()

    /**
     * Tela somente-leitura "Formação dos Times".
     *
     * Aberta diretamente do EventDetail por usuários comuns (que não são donos do
     * grupo nem criadores do evento). O popback retorna direto para EventDetail —
     * essa rota não passa pelo TeamsScreen de gerenciamento.
     */
    data class TeamsReport(
        val eventId: String,
        val eventName: String
    ) : AppRoute()
}