package app.loobby.feature.gameplays.domain.model

/** Como o jogo da gameplay é decidido. [id] é o valor da API. */
enum class GameplayMode(val id: String) {
    VOTING("VOTING"),
    FIXED("FIXED"),
    FREE("FREE");

    companion object {
        fun fromId(id: String): GameplayMode = entries.firstOrNull { it.id == id } ?: FREE
    }
}

/** Onde se joga. Por enquanto, uma por gameplay; a API já guarda uma lista. */
enum class Platform(val id: String) {
    PC("PC"),
    PLAYSTATION("PLAYSTATION"),
    XBOX("XBOX"),
    SWITCH("SWITCH"),
    MOBILE("MOBILE");

    companion object {
        fun fromId(id: String): Platform? = entries.firstOrNull { it.id == id }
    }
}

/** Vou, Não vou ou Pendente. Votar implica Vou; quem marcou Não vou não vota. */
enum class ParticipationStatus(val id: String) {
    GOING("GOING"),
    NOT_GOING("NOT_GOING"),
    PENDING("PENDING");

    companion object {
        fun fromId(id: String): ParticipationStatus = entries.firstOrNull { it.id == id } ?: PENDING
    }
}

/** Ativa até o Encerrar ou até 24 h depois do horário marcado. */
enum class GameplayStatus {
    ACTIVE,
    ENDED;

    companion object {
        fun fromId(id: String): GameplayStatus = if (id == "ENDED") ENDED else ACTIVE
    }
}
