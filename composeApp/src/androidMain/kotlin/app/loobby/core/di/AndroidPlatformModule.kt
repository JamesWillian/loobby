package app.loobby.core.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android
import org.koin.dsl.module

val androidPlatformModule = module {
    single<HttpClientEngine> { Android.create() }
}