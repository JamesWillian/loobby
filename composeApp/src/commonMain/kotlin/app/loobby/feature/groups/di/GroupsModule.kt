package app.loobby.feature.groups.di

import app.loobby.feature.groups.data.remote.GroupsApi
import app.loobby.feature.groups.data.remote.GroupsApiImpl
import app.loobby.feature.groups.data.repository.GroupsRepositoryImpl
import app.loobby.feature.groups.domain.repository.GroupsRepository
import app.loobby.feature.groups.domain.usecase.*
import app.loobby.feature.groups.presentation.GroupsViewModel
import io.ktor.client.HttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.module

val groupsModule = module {

    // ── Groups API ──────────────────────────────────────────────────
    single<GroupsApi> {
        val client: HttpClient = get(named("authedClient"))
        GroupsApiImpl(client)
    }

    single<GroupsRepository> { GroupsRepositoryImpl(get(), get(), get()) }

    factory { CreateGroupUseCase(get()) }
    factory { ListMyGroupsUseCase(get()) }
    factory { GetGroupByIdUseCase(get()) }
    factory { GetGroupByInviteUseCase(get()) }
    factory { JoinGroupUseCase(get()) }
    factory { LeaveGroupUseCase(get()) }
    factory { ListGroupMembersUseCase(get()) }
    factory { UpdateGroupUseCase(get()) }
    factory { UploadGroupImageUseCase(get()) }
    factory { DeleteGroupUseCase(get()) }
    factory { RemoveGroupMemberUseCase(get()) }

    // ── ViewModels ──────────────────────────────────────────────────
    single {
        GroupsViewModel(
            createGroup = get(),
            listMyGroups = get(),
            getGroupById = get(),
            leaveGroup = get(),
            listMembers = get(),
            updateGroupUseCase = get(),
            uploadGroupImageUseCase = get(),
            deleteGroupUseCase = get(),
            removeMemberUseCase = get(),
            authRepository = get(),
            imagePrefetcher = get(),
            analytics = get(),
            crashReporter = get()
        )
    }
}
