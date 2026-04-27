package app.loobby.core.di

import app.loobby.core.db.DatabaseDriverFactory
import app.loobby.core.network.ConnectivityObserver
import coil3.PlatformContext
import com.russhwolf.settings.Settings
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.dsl.module

val iosPlatformModule = module {
    single<HttpClientEngine> { Darwin.create() }

    single<ConnectivityObserver> { ConnectivityObserver() }

    // No iOS/non-Android, `PlatformContext` é uma classe com companion object no Coil,
    // então a instância singleton é acessada via `PlatformContext.INSTANCE`.
    single<PlatformContext> { PlatformContext.INSTANCE }

    // SQLDelight — NativeSqliteDriver não precisa de context; o sandbox já
    // dá o diretório. Construtor vazio.
    single<DatabaseDriverFactory> { DatabaseDriverFactory() }

    // Em iOS, multiplatform-settings-no-arg usa NSUserDefaults por baixo.
    // O sandbox do app isola o arquivo, então é seguro o suficiente para
    // tokens. Se um dia quisermos endurecer, trocar para um wrapper de
    // Keychain.
    single<Settings> { Settings() }
}
