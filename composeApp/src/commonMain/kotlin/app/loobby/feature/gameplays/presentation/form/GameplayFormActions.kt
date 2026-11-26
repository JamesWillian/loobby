package app.loobby.feature.gameplays.presentation.form

import app.loobby.feature.gameplays.domain.model.GameChoice
import app.loobby.feature.gameplays.domain.model.Platform

/**
 * O que os campos do formulário pedem. Os componentes dependem disto, e não do
 * ViewModel, para poderem ser desenhados (e testados) sozinhos.
 */
interface GameplayFormActions {
    fun backToModes()
    fun openGamePicker(target: GamePickTarget)
    fun onPlaceChange(place: PlaceOption)
    fun onNameChange(value: String)
    fun onDescriptionChange(value: String)
    fun onDateChange(digits: String)
    fun onTimeChange(digits: String)
    fun startNow()
    fun onPlatformChange(platform: Platform)
    fun onFinalistsChange(value: Int)
    fun onMultiVoteChange(value: Boolean)
    fun removeVotingGame(game: GameChoice)
    fun submit()
}
