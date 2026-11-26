package app.loobby.feature.gameplays.domain.model

/**
 * A votação. [round] 1 é a principal; 2 em diante são desempates, sempre com 1
 * voto por pessoa e só com os jogos empatados.
 */
data class Voting(
    val finalists: Int,
    val multiVote: Boolean,
    /** Finalistas e voto múltiplo já não mudam: houve voto. */
    val locked: Boolean,
    val round: Int,
    val games: List<VotingGame>,
    val result: VotingResult,
) {
    val isTiebreak: Boolean get() = round > 1

    /** No desempate, sempre 1 voto por pessoa. */
    val allowsMultipleVotes: Boolean get() = multiVote && !isTiebreak

    /** Os jogos que concorrem na rodada aberta. */
    val candidates: List<VotingGame> get() = games.filter { it.candidate }

    /** Os votos da pessoa que lê, na rodada aberta. */
    val myVotes: Set<String> get() = games.filter { it.myVote }.map { it.id }.toSet()

    fun votesInOpenRound(game: VotingGame): RoundVotes? = game.rounds.firstOrNull { it.round == round }

    fun isFinalist(game: VotingGame): Boolean = game.id in result.finalistIds
}

/**
 * Um jogo da votação. [id] é o desta entrada (o que vai no voto); o do catálogo
 * está em [catalogId].
 */
data class VotingGame(
    val id: String,
    val catalogId: String?,
    val name: String,
    val coverUrl: String?,
    val addedBy: String?,
    /** Concorre na rodada aberta. */
    val candidate: Boolean,
    /** Quem lê votou nele na rodada aberta. */
    val myVote: Boolean,
    /** Uma entrada por rodada em que concorreu. */
    val rounds: List<RoundVotes>,
)

data class RoundVotes(
    val round: Int,
    val votes: Int,
    val voters: List<GameplayUser>,
)

data class VotingResult(
    val round: Int,
    val finalistIds: List<String>,
    /** Com 1 finalista: empate no 1º lugar. É o que abre o Desempatar. */
    val tie: Boolean,
)
