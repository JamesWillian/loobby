package app.loobby.core.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.dsl.module

val iosPlatformModule = module {
    single<HttpClientEngine> { Darwin.create() }
}