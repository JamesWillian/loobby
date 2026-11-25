package app.loobby.feature.gameplays.domain.model

import kotlin.time.Instant

data class GameplayComment(
    val id: String,
    val gameplayId: String,
    val author: GameplayUser,
    val body: String,
    val createdAt: Instant,
    val canDelete: Boolean,
    /** Quem lê denunciou: o app esconde o comentário dessa pessoa. */
    val reportedByMe: Boolean,
)

/** Uma página de comentários, do mais novo para o mais antigo. */
data class CommentsPage(
    val comments: List<GameplayComment>,
    /** Veio a página cheia: pode haver mais antigos. */
    val mayHaveMore: Boolean,
)
