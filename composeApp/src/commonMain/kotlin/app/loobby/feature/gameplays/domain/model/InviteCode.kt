package app.loobby.feature.gameplays.domain.model

/** De que é um código de convite. Os formatos são todos "$-" + alguma coisa. */
enum class InviteKind {
    /** "$-" + 6: grupo. */
    GROUP,

    /** "$-" + 8: gameplay única. */
    GAMEPLAY,

    /** "$-XXXX-XXXX": evento da versão antiga, que a 2.0 não abre. */
    LEGACY_EVENT,

    UNKNOWN,
}

object InviteCode {
    const val PREFIX = "$-"

    private const val GROUP_LENGTH = 6
    private const val GAMEPLAY_LENGTH = 8
    private val legacyEvent = Regex("^[A-Z0-9]{4}-[A-Z0-9]{4}$")

    /** O código como a API espera: com o prefixo, sem espaços, em maiúsculas. */
    fun normalize(code: String): String = PREFIX + suffixOf(code)

    fun kindOf(code: String): InviteKind {
        val suffix = suffixOf(code)
        val plain = suffix.all { it in 'A'..'Z' || it in '0'..'9' }
        return when {
            plain && suffix.length == GROUP_LENGTH -> InviteKind.GROUP
            plain && suffix.length == GAMEPLAY_LENGTH -> InviteKind.GAMEPLAY
            legacyEvent.matches(suffix) -> InviteKind.LEGACY_EVENT
            else -> InviteKind.UNKNOWN
        }
    }

    private fun suffixOf(code: String): String = code.trim().uppercase().removePrefix(PREFIX)
}
