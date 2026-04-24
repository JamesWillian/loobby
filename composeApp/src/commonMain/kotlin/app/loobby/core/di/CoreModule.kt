package app.loobby.core.di

import app.loobby.core.db.DatabaseDriverFactory
import app.loobby.core.media.ImagePrefetcher
import app.loobby.core.navigation.DeepLinkCoordinator
import app.loobby.core.network.ConnectivityObserver
import app.loobby.core.network.HttpClientFactory
import app.loobby.core.preferences.UserPreferencesRepository
import app.loobby.core.storage.SettingsTokenStorage
import app.loobby.core.storage.TokenStorage
import app.loobby.db.LoobbyDatabase
import coil3.PlatformContext
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

    // Observador de conectividade de rede — também é fornecido em cada
    // plataforma (Android precisa de Context, iOS é construtor vazio).
    single<ConnectivityObserver> {
        error("ConnectivityObserver não registrado para esta plataforma")
    }

    // PlatformContext do Coil — fornecido por plataforma. Android usa o
    // applicationContext; iOS usa o singleton do Coil.
    single<PlatformContext> {
        error("PlatformContext não registrado para esta plataforma")
    }

    // Prefetcher de imagens — aquece o disk cache para garantir que
    // imagens do feed/grupos/eventos fiquem disponíveis offline.
    single { ImagePrefetcher(get()) }

    // DatabaseDriverFactory é provido por cada plataforma (Android precisa
    // de Context; iOS é construtor vazio).
    single<DatabaseDriverFactory> {
        error("DatabaseDriverFactory não registrado para esta plataforma")
    }

    // Banco SQLDelight — um único LoobbyDatabase por processo. Os queries
    // individuais são acessados via `database.<tabela>Queries` nos repos.
    single<LoobbyDatabase> { LoobbyDatabase(get<DatabaseDriverFactory>().createDriver()) }

    single { Settings() }

    single<TokenStorage> { SettingsTokenStorage(get()) }

    single { UserPreferencesRepository(get()) }

    single { DeepLinkCoordinator() }

    // HttpClient sem Authorization automático
    single<HttpClient>(named("baseClient")) {
        HttpClientFactory.createBaseClient(get())
    }

    // HttpClient que já injeta Authorization se tiver token salvo
    single<HttpClient>(named("authedClient")) {
        HttpClientFactory.createAuthedClient(get(), get())
    }
}