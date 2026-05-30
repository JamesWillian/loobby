package app.loobby.feature.events.di

import app.loobby.feature.events.data.remote.EventsApi
import app.loobby.feature.events.data.remote.EventsApiImpl
import app.loobby.feature.events.data.repository.EventsRepositoryImpl
import app.loobby.feature.events.domain.repository.EventsRepository
import app.loobby.feature.events.domain.usecase.UpsertRsvpUseCase
import app.loobby.feature.events.domain.usecase.CreateGroupEventUseCase
import app.loobby.feature.events.domain.usecase.CreateInstantEventUseCase
import app.loobby.feature.events.domain.usecase.DeleteEventUseCase       // import novo
import app.loobby.feature.events.domain.usecase.DeleteMyRsvpUseCase      // import novo (remover presença)
import app.loobby.feature.events.domain.usecase.GetEventByIdUseCase
import app.loobby.feature.events.domain.usecase.GetEventByInviteUseCase
import app.loobby.feature.events.domain.usecase.GetGroupEventsUseCase
import app.loobby.feature.events.domain.usecase.GetMyRsvpUseCase
import app.loobby.feature.events.domain.usecase.ListEventRsvpsUseCase
import app.loobby.feature.events.domain.usecase.UpdateEventUseCase       // import novo
import app.loobby.feature.events.presentation.CreateEventViewModel
import app.loobby.feature.events.presentation.EventDetailViewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val eventsModule = module {

    single<EventsApi> {
        EventsApiImpl(client = get(named("authedClient")))
    }

    single<EventsRepository> {
        EventsRepositoryImpl(get(), get(), get())
    }

    factory { GetGroupEventsUseCase(get()) }
    factory { UpsertRsvpUseCase(get()) }
    factory { CreateGroupEventUseCase(get()) }
    factory { CreateInstantEventUseCase(get()) }
    factory { GetEventByIdUseCase(get()) }
    factory { GetEventByInviteUseCase(get()) }
    factory { ListEventRsvpsUseCase(get()) }
    factory { GetMyRsvpUseCase(get()) }
    factory { UpdateEventUseCase(get()) }    // novo use case
    factory { DeleteEventUseCase(get()) }    // novo use case
    factory { DeleteMyRsvpUseCase(get()) }   // novo use case (remover presença)

    single {
        CreateEventViewModel(
            createGroupEvent = get(),
            createInstantEvent = get(),
            updateEvent = get(),             // novo parâmetro
            searchGames = get(),             // gamesModule
            saveGame = get()                 // gamesModule
        )
    }

    single {
        EventDetailViewModel(
            getEventById = get(),
            listRsvps = get(),
            upsertRsvp = get(),
            getMyRsvp = get(),
            deleteEvent = get(),             // novo parâmetro
            deleteMyRsvp = get(),            // novo parâmetro (remover presença)
            authRepository = get(),          // novo parâmetro
            listGroupMembers = get(),        // novo parâmetro (vem do groupsModule)
            getGame = get(),                 // gamesModule — capa do jogo (RAWG)
            imagePrefetcher = get()
        )
    }
}