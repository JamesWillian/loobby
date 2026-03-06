package app.loobby.core.di

import app.loobby.core.network.HttpClientFactory
import app.loobby.core.preferences.UserPreferencesRepository
import app.loobby.core.storage.SettingsTokenStorage
import app.loobby.core.storage.TokenStorage
import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import org.koin.core.qualifier.named
import org.koin.dsl.module

val coreModule = module {

    // Engine é fornecido em cada plataforma (androidMain/iosMain)
    single<HttpClientEngine> {
        error("HttpClientEngine não registrado para esta plataforma")
    }

    single { Settings() }

    single<TokenStorage> { SettingsTokenStorage(get()) }

    single { UserPreferencesRepository(get()) }

    // HttpClient sem Authorization automático
    single<HttpClient>(named("baseClient")) {
        HttpClientFactory.createBaseClient(get())
    }

    // HttpClient que já injeta Authorization se tiver token salvo
    single<HttpClient>(named("authedClient")) {
        HttpClientFactory.createAuthedClient(get(), get())
    }
}