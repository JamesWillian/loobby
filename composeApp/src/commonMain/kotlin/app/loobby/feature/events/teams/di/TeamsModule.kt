package app.loobby.feature.events.teams.di

import app.loobby.feature.events.teams.data.remote.TeamsApi
import app.loobby.feature.events.teams.data.remote.TeamsApiImpl
import app.loobby.feature.events.teams.data.repository.TeamsRepositoryImpl
import app.loobby.feature.events.teams.domain.repository.TeamsRepository
import app.loobby.feature.events.teams.domain.usecase.*
import app.loobby.feature.events.teams.presentation.TeamsViewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val teamsModule = module {

    single<TeamsApi> {
        TeamsApiImpl(client = get(named("authedClient")))
    }

    single<TeamsRepository> {
        TeamsRepositoryImpl(get())
    }

    factory { ListTeamsUseCase(get()) }
    factory { CreateTeamUseCase(get()) }
    factory { UpdateTeamUseCase(get()) }
    factory { DeleteTeamUseCase(get()) }
    factory { AddPlayerToTeamUseCase(get()) }
    factory { RemovePlayerFromTeamUseCase(get()) }
    factory { MovePlayerUseCase(get()) }
    factory { AutoGenerateTeamsUseCase(get()) }

    single {
        TeamsViewModel(
            listTeams = get(),
            createTeam = get(),
            updateTeam = get(),
            deleteTeam = get(),
            addPlayer = get(),
            removePlayer = get(),
            movePlayer = get(),
            autoGenerate = get(),
            listRsvps = get()  // vem do eventsModule
        )
    }
}