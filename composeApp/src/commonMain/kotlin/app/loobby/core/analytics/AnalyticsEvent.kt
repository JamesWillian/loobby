package app.loobby.core.analytics

/**
 * Ações do usuário registradas no Analytics. Os parâmetros só levam valores fixos,
 * escritos no código (método de login, modo da gameplay, de onde veio a ação). Nenhum
 * evento carrega nome, e-mail, apelido, texto digitado, código de convite, nome de
 * grupo, de gameplay ou de jogo, nem id de usuário, grupo ou gameplay.
 *
 * Os eventos das gameplays ficam em GameplayAnalyticsEvents.kt.
 */
sealed class AnalyticsEvent(
    val name: String,
    val params: Map<String, String> = emptyMap()
) {
    /** Uma tela ou um sheet abriu. O nome e o parâmetro são os que o Analytics usa para contar telas. */
    data class ScreenView(val screen: AppScreen) : AnalyticsEvent(
        name = "screen_view",
        params = mapOf("screen_name" to screen.id)
    )

    // ─── Conta ──────────────────────────────────────────────────────────────

    data class Login(val method: AuthMethod) : AnalyticsEvent(
        name = "login",
        params = mapOf("method" to method.id)
    )

    /** O login não passou: senha errada, falha de rede ou erro do servidor. Não leva o motivo. */
    data class LoginFailed(val method: AuthMethod) : AnalyticsEvent(
        name = "login_failed",
        params = mapOf("method" to method.id)
    )

    /** A conta anônima virou conta registrada, com e-mail e senha. */
    data object SignUp : AnalyticsEvent("sign_up", mapOf("method" to AuthMethod.EMAIL.id))

    data object SignUpFailed : AnalyticsEvent("sign_up_failed")

    data object Logout : AnalyticsEvent("logout")

    data object AccountDeleted : AnalyticsEvent("account_deleted")

    data object PasswordResetRequested : AnalyticsEvent("password_reset_requested")

    data object VerificationEmailResent : AnalyticsEvent("verification_email_resent")

    /** Username ou nome de exibição alterados (inclui o apelido do usuário anônimo). */
    data object ProfileUpdated : AnalyticsEvent("profile_updated")

    data object AvatarUpdated : AnalyticsEvent("avatar_updated")

    data object PasswordChanged : AnalyticsEvent("password_changed")

    // ─── Grupos ─────────────────────────────────────────────────────────────

    data object GroupCreated : AnalyticsEvent("group_created")

    /** Entrou em um grupo pelo código de convite. */
    data object GroupJoined : AnalyticsEvent("join_group")

    data object GroupLeft : AnalyticsEvent("group_left")

    data object GroupRenamed : AnalyticsEvent("group_renamed")

    data object GroupImageUpdated : AnalyticsEvent("group_image_updated")

    data object GroupDeleted : AnalyticsEvent("group_deleted")

    data object GroupMemberRemoved : AnalyticsEvent("group_member_removed")

    /** Leva só o que a busca achou, nunca o código digitado. */
    data class InviteCodeSearched(val result: InviteSearchResult) : AnalyticsEvent(
        name = "invite_code_searched",
        params = mapOf("result" to result.id)
    )

    data class InviteCodeCopied(val content: SharedContent) : AnalyticsEvent(
        name = "invite_code_copied",
        params = mapOf("content_type" to content.id)
    )

    // ─── Jogos ──────────────────────────────────────────────────────────────

    /** De onde veio o jogo escolhido no formulário da gameplay. O nome do jogo não vai. */
    data class GameSelected(val source: GamePickSource) : AnalyticsEvent(
        name = "game_selected",
        params = mapOf("source" to source.id)
    )
}

/** As telas e os sheets contados no Analytics: só nomes fixos. */
enum class AppScreen(val id: String) {
    LOBBY("lobby"),
    GROUP_HOME("group_home"),
    GROUP_DETAIL("group_detail"),
    GAMEPLAY_DETAIL("gameplay_detail"),
    CREATE_GAMEPLAY("create_gameplay"),
    EDIT_GAMEPLAY("edit_gameplay"),
    ADD_VOTING_GAME("add_voting_game"),
    HISTORY("history"),

    /** As Finalizadas de um grupo. */
    GROUP_ENDED_GAMEPLAYS("group_ended_gameplays"),

    /** O sheet de um jogo da biblioteca da pessoa (o "tenho o jogo"). */
    LIBRARY_GAME("library_game"),

    /** A busca para pôr um jogo na biblioteca da pessoa. */
    ADD_TO_LIBRARY("add_to_library"),

    /** A busca para pôr um jogo na biblioteca do grupo. */
    ADD_TO_GROUP_LIBRARY("add_to_group_library"),

    /** A aba Perfil. O sheet de editar o nome e o @ é [PROFILE]. */
    PROFILE_TAB("profile_tab"),
    PROFILE("profile"),
    AUTH("auth"),
    NICKNAME("nickname"),
    CREATE_GROUP("create_group"),
    JOIN_BY_INVITE("join_by_invite"),

    /** A versão instalada está abaixo da mínima do /app/config. */
    UPDATE_REQUIRED("update_required"),

    /** O aviso do /app/config. Qual aviso foi não vai junto. */
    NOTICE("notice"),
}

enum class AuthMethod(val id: String) {
    EMAIL("email"),
    GOOGLE("google")
}

enum class InviteSearchResult(val id: String) {
    GROUP("group"),

    /** Gameplay única. */
    GAMEPLAY("gameplay"),

    /** Código de evento da versão antiga, que a 2.0 não abre. */
    LEGACY_EVENT("legacy_event"),

    /** Código inexistente ou busca que falhou. */
    FAILED("failed")
}

/** O que teve o código de convite copiado. */
enum class SharedContent(val id: String) {
    GROUP("group")
}
