package app.loobby.feature.games.di

import app.loobby.feature.games.data.remote.GamesApi
import app.loobby.feature.games.data.remote.GamesApiImpl
import app.loobby.feature.games.data.repository.GamesRepositoryImpl
import app.loobby.feature.games.domain.repository.GamesRepository
import app.loobby.feature.games.domain.usecase.GetGameUseCase
import app.loobby.feature.games.domain.usecase.SaveGameUseCase
import app.loobby.feature.games.domain.usecase.SearchGamesUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

val gamesModule = module {

    single<GamesApi> {
        GamesApiImpl(client = get(named("authedClient")))
    }

    single<GamesRepository> {
        GamesRepositoryImpl(get(), get(), get())
    }

    factory { SearchGamesUseCase(get()) }
    factory { GetGameUseCase(get()) }
    factory { SaveGameUseCase(get()) }
}
