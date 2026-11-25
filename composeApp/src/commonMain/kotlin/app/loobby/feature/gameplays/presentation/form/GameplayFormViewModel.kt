package app.loobby.feature.gameplays.presentation.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.loobby.core.analytics.AnalyticsEvent
import app.loobby.core.analytics.AnalyticsTracker
import app.loobby.core.analytics.CrashReporter
import app.loobby.core.analytics.GameplayCreated
import app.loobby.core.analytics.GameplayUpdated
import app.loobby.core.analytics.reportIfUnexpected
import app.loobby.core.network.toUserMessage
import app.loobby.core.ui.components.toDigits
import app.loobby.feature.gameplays.domain.model.GameChoice
import app.loobby.feature.gameplays.domain.model.GameInfo
import app.loobby.feature.gameplays.domain.model.Gameplay
import app.loobby.feature.gameplays.domain.model.GameplayChanges
import app.loobby.feature.gameplays.domain.model.GameplayDraft
import app.loobby.feature.gameplays.domain.model.GameplayMode
import app.loobby.feature.gameplays.domain.model.GameplayRules
import app.loobby.feature.gameplays.domain.model.Platform
import app.loobby.feature.gameplays.domain.model.VotingConfig
import app.loobby.feature.gameplays.domain.usecase.CreateGameplayUseCase
import app.loobby.feature.gameplays.domain.usecase.UpdateGameplayUseCase
import app.loobby.feature.groups.domain.usecase.ListMyGroupsUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Criar e editar gameplay. Na criação, primeiro o modo, depois os detalhes; na
 * edição, só os detalhes, e vai para a API só o que mudou.
 */
class GameplayFormViewModel(
    private val formMode: GameplayFormMode,
    private val createGameplay: CreateGameplayUseCase,
    private val updateGameplay: UpdateGameplayUseCase,
    private val listMyGroups: ListMyGroupsUseCase,
    private val analytics: AnalyticsTracker,
    private val crashReporter: CrashReporter,
    private val clock: Clock = Clock.System,
    private val zone: TimeZone = TimeZone.currentSystemDefault(),
) : ViewModel(), GameplayFormActions {

    private val _state = MutableStateFlow(initialState())
    val state: StateFlow<GameplayFormUiState> = _state.asStateFlow()

    private val _saved = Channel<Gameplay>(Channel.BUFFERED)

    /** A gameplay criada ou salva, para quem abriu o formulário seguir com ela. */
    val saved: Flow<Gameplay> = _saved.receiveAsFlow()

    init {
        if (formMode is GameplayFormMode.Create && formMode.groupId == null) loadPlaces()
    }

    // ── Passos ─────────────────────────────────────────────────────────────

    fun selectMode(mode: GameplayMode) = _state.update { it.copy(mode = mode, step = FormStep.DETAILS, isDirty = true) }

    override fun backToModes() = _state.update { it.copy(step = FormStep.MODE, errorMessage = null) }

    override fun openGamePicker(target: GamePickTarget) = _state.update { it.copy(step = FormStep.PICK_GAME, pickTarget = target) }

    fun closeGamePicker() = _state.update { it.copy(step = FormStep.DETAILS, pickTarget = null) }

    fun onGamePicked(choice: GameChoice) {
        analytics.track(AnalyticsEvent.GameSelected(fromCatalog = choice.catalogId != null))
        _state.update { state ->
            when (state.pickTarget) {
                GamePickTarget.FIXED_GAME -> state.copy(
                    fixedGame = choice,
                    name = state.name.ifBlank { choice.name },
                )
                GamePickTarget.VOTING_GAME -> when {
                    GameplayFormValidator.isRepeated(state.votingGames, choice) ->
                        state.copy(errorMessage = "${choice.name} já está na votação")
                    state.votingGames.size >= GameplayRules.MAX_GAMES ->
                        state.copy(errorMessage = "Até ${GameplayRules.MAX_GAMES} jogos na votação")
                    else -> state.copy(votingGames = state.votingGames + choice, errorMessage = null)
                }
                null -> state
            }.copy(step = FormStep.DETAILS, pickTarget = null, isDirty = true)
        }
    }

    // ── Campos ─────────────────────────────────────────────────────────────

    override fun onPlaceChange(place: PlaceOption) = edit { it.copy(place = place) }
    override fun onNameChange(value: String) = edit { it.copy(name = value.take(GameplayRules.MAX_NAME)) }
    override fun onDescriptionChange(value: String) = edit { it.copy(description = value.take(GameplayRules.MAX_DESCRIPTION)) }
    override fun onDateChange(digits: String) = edit { it.copy(dateDigits = digits) }
    override fun onTimeChange(digits: String) = edit { it.copy(timeDigits = digits) }
    override fun onPlatformChange(platform: Platform) = edit { it.copy(platform = platform) }
    override fun onFinalistsChange(value: Int) = edit { it.copy(finalists = value.coerceIn(1, GameplayRules.MAX_FINALISTS)) }
    override fun onMultiVoteChange(value: Boolean) = edit { it.copy(multiVote = value) }
    override fun removeVotingGame(game: GameChoice) = edit { it.copy(votingGames = it.votingGames - game) }

    /** Agora: a API aceita até 5 min no passado, então não há corrida com o relógio. */
    override fun startNow() {
        val local = clock.now().toLocalDateTime(zone)
        val time = local.hour.toString().padStart(2, '0') + local.minute.toString().padStart(2, '0')
        edit { it.copy(dateDigits = local.date.toDigits(), timeDigits = time) }
    }

    // ── Salvar ─────────────────────────────────────────────────────────────

    override fun submit() {
        val state = _state.value
        if (state.isSaving) return
        val original = (formMode as? GameplayFormMode.Edit)?.gameplay
        val scheduleChanged = original == null || scheduleDigits(original.scheduledAt) != state.dateDigits to state.timeDigits
        val result = GameplayFormValidator.validate(state, clock.now(), zone, checkSchedule = scheduleChanged)
        if (result is GameplayFormValidator.Result.Invalid) {
            _state.update { it.copy(errorMessage = result.message) }
            return
        }
        val scheduledAt = (result as GameplayFormValidator.Result.Valid).scheduledAt
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                val gameplay = if (original == null) create(state, scheduledAt) else update(original, state, scheduledAt)
                _state.update { it.copy(isSaving = false, isDirty = false) }
                _saved.send(gameplay)
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                crashReporter.reportIfUnexpected(t)
                _state.update { it.copy(isSaving = false, errorMessage = t.toUserMessage("Não deu para salvar a gameplay.")) }
            }
        }
    }

    private suspend fun create(state: GameplayFormUiState, scheduledAt: Instant): Gameplay {
        val mode = requireNotNull(state.mode)
        val platform = requireNotNull(state.platform)
        val draft = GameplayDraft(
            groupId = state.place.groupId,
            name = state.name.trim(),
            description = state.description.trim().ifEmpty { null },
            mode = mode,
            platform = platform,
            scheduledAt = scheduledAt,
            game = state.fixedGame,
            voting = VotingConfig(finalists = state.finalists, multiVote = state.multiVote),
            games = state.votingGames,
        )
        return createGameplay(draft).also {
            analytics.track(GameplayCreated(mode, inGroup = draft.groupId != null, platform = platform))
        }
    }

    private suspend fun update(original: Gameplay, state: GameplayFormUiState, scheduledAt: Instant): Gameplay {
        val changes = changesBetween(original, state, scheduledAt)
        if (changes.isEmpty) return original
        return updateGameplay(original.id, changes).also { analytics.track(GameplayUpdated(original.mode)) }
    }

    /** Só o que a pessoa mudou: a API recusa mexer no que travou (a votação com voto). */
    private fun changesBetween(original: Gameplay, state: GameplayFormUiState, scheduledAt: Instant): GameplayChanges {
        val name = state.name.trim()
        val description = state.description.trim()
        val voting = original.voting
        val votingChanged = voting != null && state.canConfigureVoting &&
            (voting.finalists != state.finalists || voting.multiVote != state.multiVote)
        val game = state.fixedGame
        return GameplayChanges(
            name = name.takeIf { it != original.name },
            description = description.takeIf { it != original.description.orEmpty() },
            scheduledAt = scheduledAt.takeIf { scheduleDigits(original.scheduledAt) != state.dateDigits to state.timeDigits },
            platform = state.platform?.takeIf { it != original.platform },
            game = game?.takeIf { original.mode == GameplayMode.FIXED && it.differsFrom(original.game) },
            voting = if (votingChanged) VotingConfig(state.finalists, state.multiVote) else null,
        )
    }

    // ── Apoio ──────────────────────────────────────────────────────────────

    private fun GameChoice.differsFrom(current: GameInfo?): Boolean =
        current == null || name != current.name || catalogId != current.catalogId

    private fun edit(change: (GameplayFormUiState) -> GameplayFormUiState) =
        _state.update { change(it).copy(isDirty = true, errorMessage = null) }

    private fun loadPlaces() {
        viewModelScope.launch {
            runCatching { listMyGroups() }
                .onSuccess { groups ->
                    val options = listOf(PlaceOption.Single) + groups.map { PlaceOption(it.id, it.name) }
                    _state.update { it.copy(placeOptions = options) }
                }
                .onFailure(crashReporter::reportIfUnexpected)
        }
    }

    private fun initialState(): GameplayFormUiState = when (val mode = formMode) {
        is GameplayFormMode.Create -> {
            val fixedPlace = mode.groupId?.let { PlaceOption(it, mode.groupName ?: "Grupo") }
            GameplayFormUiState(
                place = fixedPlace ?: PlaceOption.Single,
                isPlaceLocked = fixedPlace != null,
                placeOptions = listOf(fixedPlace ?: PlaceOption.Single),
            )
        }
        is GameplayFormMode.Edit -> editState(mode.gameplay)
    }

    private fun editState(gameplay: Gameplay): GameplayFormUiState {
        val (date, time) = scheduleDigits(gameplay.scheduledAt)
        val place = PlaceOption(gameplay.groupId, gameplay.groupName ?: PlaceOption.Single.label)
        return GameplayFormUiState(
            isEdit = true,
            step = FormStep.DETAILS,
            mode = gameplay.mode,
            place = place,
            isPlaceLocked = true,
            placeOptions = listOf(place),
            name = gameplay.name,
            description = gameplay.description.orEmpty(),
            dateDigits = date,
            timeDigits = time,
            platform = gameplay.platform,
            fixedGame = gameplay.game?.let { GameChoice(it.catalogId, it.name, it.coverUrl) },
            finalists = gameplay.voting?.finalists ?: 1,
            multiVote = gameplay.voting?.multiVote ?: false,
            canConfigureVoting = gameplay.permissions.configureVoting,
        )
    }

    /** O horário como o formulário guarda: DDMMAAAA e HHMM, no fuso do aparelho. */
    private fun scheduleDigits(instant: Instant): Pair<String, String> {
        val local = instant.toLocalDateTime(zone)
        val time = local.hour.toString().padStart(2, '0') + local.minute.toString().padStart(2, '0')
        return local.date.toDigits() to time
    }
}
