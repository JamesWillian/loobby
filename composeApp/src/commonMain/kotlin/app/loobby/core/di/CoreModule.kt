package app.loobby.core.di

import app.loobby.core.analytics.AnalyticsTracker
import app.loobby.core.analytics.CrashReporter
import app.loobby.core.appconfig.AppConfigApi
import app.loobby.core.appconfig.AppConfigApiImpl
import app.loobby.core.appconfig.AppConfigStore
import app.loobby.core.appconfig.AppStatusViewModel
import app.loobby.core.appconfig.AppVersion
import app.loobby.core.appconfig.SettingsAppConfigStore
import app.loobby.core.db.DatabaseDriverFactory
import app.loobby.core.media.ImagePrefetcher
import app.loobby.core.navigation.DeepLinkCoordinator
import app.loobby.core.network.ConnectivityObserver
import app.loobby.core.network.HttpClientFactory
import app.loobby.core.storage.SettingsTokenStorage
import app.loobby.core.storage.TokenStorage
import app.loobby.db.FourPlayersDatabase
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
    // imagens dos grupos, das gameplays e dos avatares fiquem disponíveis offline.
    single { ImagePrefetcher(get()) }

    // DatabaseDriverFactory é provido por cada plataforma (Android precisa
    // de Context; iOS é construtor vazio).
    single<DatabaseDriverFactory> {
        error("DatabaseDriverFactory não registrado para esta plataforma")
    }

    // Banco SQLDelight — um único FourPlayersDatabase por processo. Os queries
    // individuais são acessados via `database.<tabela>Queries` nos repos.
    single<FourPlayersDatabase> { FourPlayersDatabase(get<DatabaseDriverFactory>().createDriver()) }

    // Settings é fornecido em cada plataforma:
    //  - Android usa EncryptedSharedPreferences (AES-GCM, master key no
    //    Android KeyStore) — definido em androidPlatformModule.
    //  - iOS usa NSUserDefaults via multiplatform-settings-no-arg —
    //    definido em iosPlatformModule. (O sandbox do app isola o arquivo;
    //    se algum dia quisermos endurecer, trocamos para Keychain.)
    single<Settings> {
        error("Settings não registrado para esta plataforma")
    }

    single<TokenStorage> { SettingsTokenStorage(get()) }

    single { DeepLinkCoordinator() }

    // Analytics e Crashlytics só existem no Android por enquanto: o
    // androidPlatformModule troca estes dois pelos do Firebase. No iOS, os
    // eventos e as falhas tratadas são descartados.
    single<AnalyticsTracker> { AnalyticsTracker { } }
    single<CrashReporter> { CrashReporter { } }

    // HttpClient sem Authorization automático
    single<HttpClient>(named("baseClient")) {
        HttpClientFactory.createBaseClient(get())
    }

    // HttpClient que já injeta Authorization se tiver token salvo
    single<HttpClient>(named("authedClient")) {
        HttpClientFactory.createAuthedClient(get(), get())
    }

    // ── Versão mínima e aviso (GET /app/config) ─────────────────────
    // A versão instalada vem de cada plataforma: versionCode no Android,
    // CFBundleVersion no iOS.
    single<AppVersion> {
        error("AppVersion não registrado para esta plataforma")
    }

    // baseClient: a rota é pública e não pode receber token vencido.
    single<AppConfigApi> { AppConfigApiImpl(get(named("baseClient"))) }

    single<AppConfigStore> { SettingsAppConfigStore(get()) }

    single {
        AppStatusViewModel(
            api = get(),
            store = get(),
            version = get(),
            crashReporter = get(),
        )
    }
}