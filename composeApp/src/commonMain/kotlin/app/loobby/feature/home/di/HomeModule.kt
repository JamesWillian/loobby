package app.loobby.feature.home.di

import app.loobby.feature.home.data.SettingsHomePreferences
import app.loobby.feature.home.domain.HomePreferences
import app.loobby.feature.home.presentation.HomeViewModel
import app.loobby.feature.home.presentation.history.HistoryViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** O Início (as preferências da barra lateral e o ViewModel dela) e o Histórico. */
val homeModule = module {
    single<HomePreferences> { SettingsHomePreferences(settings = get()) }
    viewModel {
        HomeViewModel(
            listMyGroups = get(),
            preferences = get(),
            auth = get(),
            imagePrefetcher = get(),
            crashReporter = get(),
        )
    }
    viewModel { HistoryViewModel(getHistory = get(), observeRemovals = get(), crashReporter = get()) }
}
