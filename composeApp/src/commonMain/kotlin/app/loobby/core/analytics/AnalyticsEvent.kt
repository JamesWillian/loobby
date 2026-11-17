package app.loobby.core.analytics

import app.loobby.feature.events.domain.model.EventType
import app.loobby.feature.events.domain.model.RsvpStatus
import app.loobby.feature.groups.domain.model.GroupEventFilter

/**
 * Ações do usuário registradas no Analytics. Os parâmetros só levam valores fixos,
 * escritos no código (tipo do evento, método de login, filtro escolhido). Nenhum
 * evento carrega nome, e-mail, apelido, texto digitado, código de convite, nome de
 * grupo, de evento ou de jogo, nem id de usuário, grupo ou evento.
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

    // ─── Eventos ────────────────────────────────────────────────────────────

    data class EventCreated(val type: EventType, val isInstant: Boolean) : AnalyticsEvent(
        name = "event_created",
        params = mapOf(
            "type" to type.name.lowercase(),
            "scope" to if (isInstant) "instant" else "group"
        )
    )

    data class EventUpdated(val type: EventType) : AnalyticsEvent(
        name = "event_updated",
        params = mapOf("type" to type.name.lowercase())
    )

    data object EventDeleted : AnalyticsEvent("event_deleted")

    /** Entrou em um evento instantâneo pelo código de convite. */
    data object EventJoinedByInvite : AnalyticsEvent("event_joined_by_invite")

    /** O jogo do gameplay veio do catálogo ou foi digitado. O nome do jogo não vai. */
    data class GameSelected(val fromCatalog: Boolean) : AnalyticsEvent(
        name = "game_selected",
        params = mapOf("source" to if (fromCatalog) "catalog" else "manual")
    )

    data class EventsFilterChanged(val filter: GroupEventFilter) : AnalyticsEvent(
        name = "events_filter_changed",
        params = mapOf("filter" to filter.name.lowercase())
    )

    /** O texto do evento ou dos times foi entregue ao compartilhamento do sistema. */
    data class Shared(val content: SharedContent) : AnalyticsEvent(
        name = "share",
        params = mapOf("content_type" to content.id)
    )

    /** O app abriu por uma notificação push de evento. */
    data class PushOpened(val hasGroup: Boolean) : AnalyticsEvent(
        name = "push_opened",
        params = mapOf("target" to if (hasGroup) "group_event" else "instant_event")
    )

    // ─── Presença ───────────────────────────────────────────────────────────

    data class RsvpSent(val status: RsvpStatus, val source: RsvpSource) : AnalyticsEvent(
        name = "rsvp_sent",
        params = mapOf(
            "status" to status.name.lowercase(),
            "source" to source.id
        )
    )

    data object RsvpRemoved : AnalyticsEvent("rsvp_removed")

    data class PaymentMarked(val isPaid: Boolean) : AnalyticsEvent(
        name = "payment_marked",
        params = mapOf("paid" to isPaid.toString())
    )

    /** A observação da presença foi salva. O texto não vai. */
    data object RsvpNoteSaved : AnalyticsEvent("rsvp_note_saved")

    // ─── Times ──────────────────────────────────────────────────────────────

    data class TeamsGenerated(val includeReserves: Boolean) : AnalyticsEvent(
        name = "teams_generated",
        params = mapOf("include_reserves" to includeReserves.toString())
    )

    data object TeamCreated : AnalyticsEvent("team_created")

    data object TeamUpdated : AnalyticsEvent("team_updated")

    data object TeamDeleted : AnalyticsEvent("team_deleted")

    data class TeamPlayerChanged(val change: TeamPlayerChange) : AnalyticsEvent(
        name = "team_player_changed",
        params = mapOf("action" to change.id)
    )

    data object TeamsReordered : AnalyticsEvent("teams_reordered")
}

/** As telas e os sheets contados no Analytics: só nomes fixos. */
enum class AppScreen(val id: String) {
    WELCOME("welcome"),
    GROUP("group"),
    GROUP_DETAIL("group_detail"),
    EVENT_DETAIL("event_detail"),
    TEAMS("teams"),
    TEAMS_REPORT("teams_report"),
    AUTH("auth"),
    PROFILE("profile"),
    NICKNAME("nickname"),
    CREATE_GROUP("create_group"),
    JOIN_BY_INVITE("join_by_invite"),
    CREATE_EVENT("create_event"),
    EDIT_EVENT("edit_event")
}

enum class AuthMethod(val id: String) {
    EMAIL("email"),
    GOOGLE("google")
}

enum class InviteSearchResult(val id: String) {
    GROUP("group"),
    EVENT("event"),

    /** Código inexistente ou busca que falhou. */
    FAILED("failed")
}

enum class SharedContent(val id: String) {
    GROUP("group"),
    EVENT("event"),
    TEAMS("teams")
}

/** De onde a presença foi enviada: da tela do evento ou do cartão na lista do grupo. */
enum class RsvpSource(val id: String) {
    EVENT_DETAIL("event_detail"),
    GROUP_LIST("group_list")
}

enum class TeamPlayerChange(val id: String) {
    ADDED("added"),
    REMOVED("removed"),
    MOVED("moved")
}
