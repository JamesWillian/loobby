package app.loobby.feature.games.domain.usecase

import app.loobby.feature.games.domain.model.GameDomain
import app.loobby.feature.games.domain.repository.GamesRepository

/**
 * Persiste o jogo escolhido para um evento. Use-o no fluxo de criação/edição de
 * evento de gameplay, ao confirmar a seleção do jogo.
 */
class SaveGameUseCase(private val repository: GamesRepository) {
    suspend operator fun invoke(game: GameDomain) = repository.saveGame(game)
}
