package app.loobby.feature.gameplays.presentation.model

import app.loobby.feature.games.domain.model.GameDomain
import app.loobby.feature.gameplays.domain.model.GameChoice

/** O jogo escolhido no catálogo, como a gameplay recebe. */
fun GameDomain.toGameChoice(): GameChoice = GameChoice(catalogId = id, name = name, coverUrl = backgroundImage)

/** O jogo digitado à mão: sem id do catálogo e sem capa. */
fun typedGameChoice(name: String): GameChoice = GameChoice(catalogId = null, name = name.trim())
