package app.loobby.feature.gameplays.di

import app.loobby.feature.gameplays.data.cache.GameplayCache
import app.loobby.feature.gameplays.data.remote.GameplaysApi
import app.loobby.feature.gameplays.data.remote.GameplaysApiImpl
import app.loobby.feature.gameplays.data.repository.GameplaysRepositoryImpl
import app.loobby.feature.gameplays.domain.repository.GameplaysRepository
import app.loobby.feature.gameplays.domain.usecase.AddVotingGameUseCase
import app.loobby.feature.gameplays.domain.usecase.CreateGameplayUseCase
import app.loobby.feature.gameplays.domain.usecase.DeleteCommentUseCase
import app.loobby.feature.gameplays.domain.usecase.EndGameplayUseCase
import app.loobby.feature.gameplays.domain.usecase.GetCommentsUseCase
import app.loobby.feature.gameplays.domain.usecase.GetGameplayInviteUseCase
import app.loobby.feature.gameplays.domain.usecase.GetGameplayUseCase
import app.loobby.feature.gameplays.domain.usecase.GetEndedGroupGameplaysUseCase
import app.loobby.feature.gameplays.domain.usecase.GetGroupGameplaysUseCase
import app.loobby.feature.gameplays.domain.usecase.GetHistoryUseCase
import app.loobby.feature.gameplays.domain.usecase.GetLobbyUseCase
import app.loobby.feature.gameplays.domain.usecase.JoinGameplayByInviteUseCase
import app.loobby.feature.gameplays.domain.usecase.LeaveGameplayUseCase
import app.loobby.feature.gameplays.domain.usecase.OpenTiebreakUseCase
import app.loobby.feature.gameplays.domain.usecase.PostCommentUseCase
import app.loobby.feature.gameplays.domain.usecase.RemoveVotingGameUseCase
import app.loobby.feature.gameplays.domain.usecase.ReportCommentUseCase
import app.loobby.feature.gameplays.domain.usecase.RespondToGameplayUseCase
import app.loobby.feature.gameplays.domain.usecase.UpdateGameplayUseCase
import app.loobby.feature.gameplays.domain.usecase.VoteUseCase
import app.loobby.feature.gameplays.presentation.detail.GameplayDetailUseCases
import app.loobby.feature.gameplays.presentation.detail.GameplayDetailViewModel
import app.loobby.feature.gameplays.presentation.ended.EndedGameplaysViewModel
import app.loobby.feature.gameplays.presentation.form.GameplayFormMode
import app.loobby.feature.gameplays.presentation.form.GameplayFormViewModel
import app.loobby.feature.gameplays.presentation.grouphome.GroupHomeViewModel
import app.loobby.feature.gameplays.presentation.invite.JoinInviteViewModel
import app.loobby.feature.gameplays.presentation.lobby.LobbyViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** As gameplays da 2.0: dados, casos de uso e os ViewModels das telas. */
val gameplaysModule = module {

    // ── Dados ───────────────────────────────────────────────────────
    single<GameplaysApi> { GameplaysApiImpl(client = get(named("authedClient"))) }
    single { GameplayCache(db = get()) }
    single<GameplaysRepository> {
        GameplaysRepositoryImpl(api = get(), cache = get(), connectivity = get(), auth = get())
    }

    // ── Casos de uso ────────────────────────────────────────────────
    factory { GetLobbyUseCase(get()) }
    factory { GetGroupGameplaysUseCase(get()) }
    factory { GetEndedGroupGameplaysUseCase(get()) }
    factory { GetHistoryUseCase(get()) }
    factory { GetGameplayUseCase(get()) }
    factory { CreateGameplayUseCase(get()) }
    factory { UpdateGameplayUseCase(get()) }
    factory { EndGameplayUseCase(get()) }
    factory { RespondToGameplayUseCase(get()) }
    factory { VoteUseCase(get()) }
    factory { AddVotingGameUseCase(get()) }
    factory { RemoveVotingGameUseCase(get()) }
    factory { OpenTiebreakUseCase(get()) }
    factory { LeaveGameplayUseCase(get()) }
    factory { GetGameplayInviteUseCase(get()) }
    factory { JoinGameplayByInviteUseCase(get()) }
    factory { GetCommentsUseCase(get()) }
    factory { PostCommentUseCase(get()) }
    factory { DeleteCommentUseCase(get()) }
    factory { ReportCommentUseCase(get()) }
    factory {
        GameplayDetailUseCases(
            getGameplay = get(),
            respond = get(),
            vote = get(),
            addVotingGame = get(),
            removeVotingGame = get(),
            openTiebreak = get(),
            end = get(),
            leave = get(),
            getComments = get(),
            postComment = get(),
            deleteComment = get(),
            reportComment = get(),
        )
    }

    // ── ViewModels: um por tela aberta (NavEntry) ou por sheet ─────
    viewModel { LobbyViewModel(getLobby = get(), crashReporter = get()) }
    viewModel { (groupId: String) ->
        GroupHomeViewModel(
            groupId = groupId,
            getGroupGameplays = get(),
            getGameplay = get(),
            respondToGameplay = get(),
            castVote = get(),
            listMembers = get(),
            analytics = get(),
            crashReporter = get(),
        )
    }
    viewModel { (groupId: String) ->
        EndedGameplaysViewModel(groupId = groupId, getEnded = get(), crashReporter = get())
    }
    viewModel { (gameplayId: String) ->
        GameplayDetailViewModel(gameplayId = gameplayId, useCases = get(), analytics = get(), crashReporter = get())
    }
    viewModel { (mode: GameplayFormMode) ->
        GameplayFormViewModel(
            formMode = mode,
            createGameplay = get(),
            updateGameplay = get(),
            listMyGroups = get(),
            analytics = get(),
            crashReporter = get(),
        )
    }
    viewModel {
        JoinInviteViewModel(
            getGroupByInvite = get(),
            joinGroup = get(),
            getGameplayInvite = get(),
            joinGameplay = get(),
            analytics = get(),
            crashReporter = get(),
        )
    }
}
