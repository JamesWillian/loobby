package app.loobby.feature.events.di

import app.loobby.feature.events.data.remote.EventsApi
import app.loobby.feature.events.data.remote.EventsApiImpl
import app.loobby.feature.events.data.repository.EventsRepositoryImpl
import app.loobby.feature.events.domain.repository.EventsRepository
import app.loobby.feature.events.domain.usecase.UpsertRsvpUseCase
import app.loobby.feature.events.domain.usecase.CreateGroupEventUseCase
import app.loobby.feature.events.domain.usecase.CreateInstantEventUseCase
import app.loobby.feature.events.domain.usecase.GetEventByIdUseCase
import app.loobby.feature.events.domain.usecase.GetGroupEventsUseCase
import app.loobby.feature.events.domain.usecase.ListEventRsvpsUseCase
import app.loobby.feature.events.presentation.CreateEventViewModel
import app.loobby.feature.events.presentation.EventDetailViewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val eventsModule = module {

    single<EventsApi> {
        EventsApiImpl(client = get(named("authedClient")))
    }

    single<EventsRepository> {
        EventsRepositoryImpl(get())
    }

    factory { GetGroupEventsUseCase(get()) }
    factory { UpsertRsvpUseCase(get()) }
    factory { CreateGroupEventUseCase(get()) }
    factory { CreateInstantEventUseCase(get()) }
    factory { GetEventByIdUseCase(get()) }
    factory { ListEventRsvpsUseCase(get()) }

    single {
        CreateEventViewModel(
            createGroupEvent = get(),
            createInstantEvent = get()
        )
    }

    single {
        EventDetailViewModel(
            getEventById = get(),
            listRsvps = get(),
            upsertRsvp = get()
        )
    }
}