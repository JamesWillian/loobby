package app.loobby.feature.library.di

import app.loobby.feature.library.data.cache.LibraryCache
import app.loobby.feature.library.data.remote.LibraryApi
import app.loobby.feature.library.data.remote.LibraryApiImpl
import app.loobby.feature.library.data.repository.LibraryRepositoryImpl
import app.loobby.feature.library.domain.model.LibrarySource
import app.loobby.feature.library.domain.repository.LibraryRepository
import app.loobby.feature.library.domain.usecase.AddGroupLibraryGameUseCase
import app.loobby.feature.library.domain.usecase.GetGroupLibraryUseCase
import app.loobby.feature.library.domain.usecase.GetLibraryPicksUseCase
import app.loobby.feature.library.domain.usecase.GetMyLibraryUseCase
import app.loobby.feature.library.domain.usecase.RemoveGroupLibraryGameUseCase
import app.loobby.feature.library.domain.usecase.RemoveLibraryGameUseCase
import app.loobby.feature.library.domain.usecase.SaveLibraryGameUseCase
import app.loobby.feature.library.presentation.group.GroupLibraryViewModel
import app.loobby.feature.library.presentation.mine.MyLibraryViewModel
import app.loobby.feature.library.presentation.picks.LibraryPicksViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** As bibliotecas de jogos da 2.0: a da pessoa, a do grupo e a escolha rápida. */
val libraryModule = module {

    // ── Dados ───────────────────────────────────────────────────────
    single<LibraryApi> { LibraryApiImpl(client = get(named("authedClient"))) }
    single { LibraryCache(db = get()) }
    single<LibraryRepository> {
        LibraryRepositoryImpl(api = get(), cache = get(), connectivity = get(), auth = get())
    }

    // ── Casos de uso ────────────────────────────────────────────────
    factory { GetMyLibraryUseCase(get()) }
    factory { SaveLibraryGameUseCase(get()) }
    factory { RemoveLibraryGameUseCase(get()) }
    factory { GetGroupLibraryUseCase(get()) }
    factory { AddGroupLibraryGameUseCase(get()) }
    factory { RemoveGroupLibraryGameUseCase(get()) }
    factory { GetLibraryPicksUseCase(get()) }

    // ── ViewModels ──────────────────────────────────────────────────
    viewModel {
        MyLibraryViewModel(
            getMyLibrary = get(),
            saveGame = get(),
            removeGame = get(),
            analytics = get(),
            crashReporter = get(),
        )
    }
    viewModel { (groupId: String) ->
        GroupLibraryViewModel(
            groupId = groupId,
            getGroupLibrary = get(),
            addGame = get(),
            removeGame = get(),
            analytics = get(),
            crashReporter = get(),
        )
    }
    viewModel { (source: LibrarySource) -> LibraryPicksViewModel(source = source, getPicks = get(), crashReporter = get()) }
}
