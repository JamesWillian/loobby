package app.loobby.feature.gameplays.domain.model

/** Quem aparece numa gameplay: quem criou, quem participa, quem votou, quem comentou. */
data class GameplayUser(
    val id: String,
    val username: String,
    val displayname: String,
    val avatarUrl: String?,
) {
    /** O nome a mostrar: o de exibição, ou o @ quando ele está vazio. */
    val shownName: String get() = displayname.ifBlank { username }
}

/**
 * Um jogo. [catalogId] é o id do catálogo (RAWG); null quando foi digitado à mão,
 * e aí não há capa.
 */
data class GameInfo(
    val catalogId: String?,
    val name: String,
    val coverUrl: String?,
)

/** O que a pessoa escolheu como jogo: do catálogo, ou só um nome digitado. */
data class GameChoice(
    val catalogId: String?,
    val name: String,
    val coverUrl: String? = null,
)
