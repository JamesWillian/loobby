package app.loobby.core.di

import app.loobby.LoobbyApplication
import app.loobby.core.db.DatabaseDriverFactory
import app.loobby.core.network.ConnectivityObserver
import coil3.PlatformContext
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android
import org.koin.dsl.module

val androidPlatformModule = module {
    single<HttpClientEngine> { Android.create() }

    // ConnectivityObserver precisa de Context — usa o application context
    // exposto pelo LoobbyApplication (já inicializado em onCreate).
    single<ConnectivityObserver> { ConnectivityObserver(LoobbyApplication.appContext) }

    // PlatformContext do Coil no Android é um alias para android.content.Context.
    // Usamos o applicationContext para não vazar Activity.
    single<PlatformContext> { LoobbyApplication.appContext }

    // SQLDelight — AndroidSqliteDriver precisa do Context (para abrir o
    // arquivo em /data/data/<pkg>/databases/).
    single<DatabaseDriverFactory> { DatabaseDriverFactory(LoobbyApplication.appContext) }
}
