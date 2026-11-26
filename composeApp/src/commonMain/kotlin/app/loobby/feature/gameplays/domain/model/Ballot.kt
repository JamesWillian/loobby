package app.loobby.feature.gameplays.domain.model

/** O que acontece com os votos da pessoa quando ela toca num jogo da cédula. */
object Ballot {

    /**
     * Os votos depois do toque em [tapped]:
     * - tocar num jogo já votado tira o voto dele;
     * - no voto múltiplo, marca mais um;
     * - no voto único, troca o voto para ele.
     */
    fun afterTap(current: Set<String>, tapped: String, multiVote: Boolean): List<String> = when {
        tapped in current -> (current - tapped).toList()
        multiVote -> (current + tapped).toList()
        else -> listOf(tapped)
    }
}
