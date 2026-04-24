package app.loobby.core.di

import app.loobby.core.db.DatabaseDriverFactory
import app.loobby.core.network.ConnectivityObserver
import coil3.PlatformContext
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.dsl.module

val iosPlatformModule = module {
    single<HttpClientEngine> { Darwin.create() }

    single<ConnectivityObserver> { ConnectivityObserver() }

    // No iOS/non-Android, `PlatformContext` é declarado como `object` no Coil,
    // então o próprio identificador já é a instância singleton.
    single<PlatformContext> { PlatformContext }

    // SQLDelight — NativeSqliteDriver não precisa de context; o sandbox já
    // dá o diretório. Construtor vazio.
    single<DatabaseDriverFactory> { DatabaseDriverFactory() }
}
