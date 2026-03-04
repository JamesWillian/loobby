package app.loobby.feature.groups.di

import app.loobby.feature.auth.data.remote.AuthApi
import app.loobby.feature.auth.data.remote.AuthApiImpl
import app.loobby.feature.groups.data.remote.GroupsApi
import app.loobby.feature.groups.data.remote.GroupsApiImpl
import app.loobby.feature.groups.data.repository.GroupsRepositoryImpl
import app.loobby.feature.groups.domain.repository.GroupsRepository
import app.loobby.feature.groups.domain.usecase.*
import app.loobby.feature.groups.presentation.GroupEventsViewModel
import app.loobby.feature.groups.presentation.GroupsViewModel
import io.ktor.client.HttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.module

val groupsModule = module {

    // Groups API usa o authedClient
    single<GroupsApi> {
        val client: HttpClient = get(named("authedClient"))
        GroupsApiImpl(client)
    }

    // AuthApi (para refresh) precisa do baseClient (sem auth header)
    // Se você já tem AuthApi registrado no authModule, pode remover esse single e só fazer get<AuthApi>()
//    single<AuthApi> {
//        val client: HttpClient = get(named("baseClient"))
//        AuthApiImpl(client)
//    }

    single<GroupsRepository> { GroupsRepositoryImpl(get(), get(), get()) }

    factory { CreateGroupUseCase(get()) }
    factory { ListMyGroupsUseCase(get()) }
    factory { GetGroupByIdUseCase(get()) }
    factory { JoinGroupUseCase(get()) }
    factory { LeaveGroupUseCase(get()) }
    factory { ListGroupMembersUseCase(get()) }

    single {
        GroupsViewModel(
            createGroup = get(),
            listMyGroups = get(),
            getGroupById = get(),
            joinGroup = get(),
            leaveGroup = get(),
            listMembers = get()
        )
    }
    single {
        GroupEventsViewModel()
    }
}