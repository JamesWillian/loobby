package app.loobby.feature.groups.di

import app.loobby.feature.groups.data.remote.GroupsApi
import app.loobby.feature.groups.data.remote.GroupsApiImpl
import app.loobby.feature.groups.data.remote.UserFeedApi
import app.loobby.feature.groups.data.remote.UserFeedApiImpl
import app.loobby.feature.groups.data.repository.GroupsRepositoryImpl
import app.loobby.feature.groups.data.repository.UserFeedRepositoryImpl
import app.loobby.feature.groups.domain.repository.GroupsRepository
import app.loobby.feature.groups.domain.repository.UserFeedRepository
import app.loobby.feature.groups.domain.usecase.*
import app.loobby.feature.groups.presentation.FeedViewModel
import app.loobby.feature.groups.presentation.GroupEventsViewModel
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

    single<GroupsRepository> { GroupsRepositoryImpl(get()) }

    // ── User Feed API ───────────────────────────────────────────────
    single<UserFeedApi> {
        val client: HttpClient = get(named("authedClient"))
        UserFeedApiImpl(client)
    }

    single<UserFeedRepository> { UserFeedRepositoryImpl(get()) }

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
    factory { ListMyFeedUseCase(get()) }

    // ── ViewModels ──────────────────────────────────────────────────
    single {
        FeedViewModel(
            listMyFeedUseCase = get(),
            prefs = get(),
            authRepository = get()
        )
    }

    single {
        GroupsViewModel(
            createGroup = get(),
            listMyGroups = get(),
            getGroupById = get(),
            joinGroup = get(),
            leaveGroup = get(),
            listMembers = get(),
            getGroupByInvite = get(),
            updateGroupUseCase = get(),
            uploadGroupImageUseCase = get(),
            deleteGroupUseCase = get(),
            removeMemberUseCase = get(),
            authRepository = get(),
            feedVm = get()
        )
    }

    single {
        GroupEventsViewModel(
            getGroupEvents = get(),
            confirmRsvp = get()
        )
    }
}