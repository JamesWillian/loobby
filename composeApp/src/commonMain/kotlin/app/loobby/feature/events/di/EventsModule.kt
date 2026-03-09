package app.loobby.feature.events.di

import app.loobby.feature.events.data.remote.EventsApi
import app.loobby.feature.events.data.remote.EventsApiImpl
import app.loobby.feature.events.data.repository.EventsRepositoryImpl
import app.loobby.feature.events.domain.repository.EventsRepository
import app.loobby.feature.events.domain.usecase.ConfirmRsvpUseCase
import app.loobby.feature.events.domain.usecase.CreateGroupEventUseCase
import app.loobby.feature.events.domain.usecase.GetGroupEventsUseCase
import app.loobby.feature.events.presentation.CreateEventViewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val eventsModule = module {

    single<EventsApi> {
        EventsApiImpl(client = get(named("authedClient")))
    }

    single<EventsRepository> {
        EventsRepositoryImpl(
            api = get(),
            authApi = get(),
            tokenStorage = get()
        )
    }

    factory { GetGroupEventsUseCase(get()) }
    factory { ConfirmRsvpUseCase(get()) }
    factory { CreateGroupEventUseCase(get()) }
    factory { CreateEventViewModel(get()) }
}