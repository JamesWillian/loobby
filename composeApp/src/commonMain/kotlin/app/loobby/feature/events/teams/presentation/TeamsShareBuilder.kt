package app.loobby.feature.events.teams.presentation

import app.loobby.feature.events.teams.domain.model.TeamDomain

/**
 * Monta o texto formatado dos times para compartilhamento via WhatsApp.
 * Usa formatação de WhatsApp: *negrito* e _itálico_.
 */
fun buildTeamsShareText(
    eventName: String?,
    teams: List<TeamDomain>
): String = buildString {

    // ── Cabeçalho ───────────────────────────────────────────────────────────
    appendLine("⚽ *Formação dos Times*")
    if (!eventName.isNullOrBlank()) {
        appendLine("📋 _${eventName}_")
    }
    appendLine()

    if (teams.isEmpty()) {
        appendLine("Nenhum time criado ainda.")
        return@buildString
    }

    // ── Times ───────────────────────────────────────────────────────────────
    teams.forEachIndexed { index, team ->
        // Emoji numérico para os primeiros 10, depois bullet
        val badge = when (index) {
            0 -> "🟥"
            1 -> "🟦"
            2 -> "🟩"
            3 -> "🟧"
            4 -> "🟪"
            5 -> "🟨"
            else -> "▪️"
        }

        appendLine("$badge *${team.name}* (${team.players.size})")

        if (team.players.isEmpty()) {
            appendLine("   _Sem jogadores_")
        } else {
            team.players.forEachIndexed { i, player ->
                val num = (i + 1).toString().padStart(2, '0')
                appendLine("  $num • ${player.displayName}")
            }
        }
        appendLine()
    }

    // ── Rodapé ──────────────────────────────────────────────────────────────
    val totalPlayers = teams.sumOf { it.players.size }
    appendLine("👥 Total: $totalPlayers jogadores em ${teams.size} times")
    appendLine()
    appendLine("_Enviado pelo Loobby_ 📲")
}