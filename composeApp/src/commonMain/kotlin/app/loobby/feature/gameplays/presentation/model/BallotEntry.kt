package app.loobby.feature.gameplays.presentation.model

import app.loobby.feature.gameplays.domain.model.Voting

/** Um jogo da cédula na rodada aberta, como a linha da votação mostra. */
data class BallotEntry(
    val votingGameId: String,
    val name: String,
    val coverUrl: String?,
    val votes: Int,
    val voterAvatars: List<String?>,
    val isMyVote: Boolean,
    val isFinalist: Boolean,
)

/** Os candidatos da rodada aberta, na ordem em que entraram na cédula. */
fun Voting.ballot(): List<BallotEntry> = candidates.map { game ->
    val openRound = votesInOpenRound(game)
    BallotEntry(
        votingGameId = game.id,
        name = game.name,
        coverUrl = game.coverUrl,
        votes = openRound?.votes ?: 0,
        voterAvatars = openRound?.voters.orEmpty().map { it.avatarUrl },
        isMyVote = game.myVote,
        isFinalist = isFinalist(game),
    )
}

/** Os [limit] mais votados, para o card; no empate, quem entrou antes vem antes. */
fun Voting.topOfBallot(limit: Int): List<BallotEntry> =
    ballot().sortedByDescending { it.votes }.take(limit)

/** Uma rodada que já congelou (antes de um desempate), com os votos de cada jogo. */
data class FrozenRound(val round: Int, val entries: List<FrozenRoundEntry>)

data class FrozenRoundEntry(val name: String, val coverUrl: String?, val votes: Int)

fun Voting.frozenRounds(): List<FrozenRound> = (1 until round).map { r ->
    FrozenRound(
        round = r,
        entries = games.mapNotNull { game ->
            game.rounds.firstOrNull { it.round == r }?.let { FrozenRoundEntry(game.name, game.coverUrl, it.votes) }
        }.sortedByDescending { it.votes },
    )
}
