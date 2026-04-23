package app.loobby.feature.notifications.di

import app.loobby.feature.notifications.data.remote.DeviceTokenApi
import app.loobby.feature.notifications.data.remote.DeviceTokenApiImpl
import app.loobby.feature.notifications.domain.repository.DeviceTokenRepository
import app.loobby.feature.notifications.domain.repository.DeviceTokenRepositoryImpl
import app.loobby.feature.notifications.domain.usecase.RegisterDeviceTokenUseCase
import app.loobby.feature.notifications.domain.usecase.UnregisterDeviceTokenUseCase
import app.loobby.feature.notifications.platform.PushTokenProvider
import app.loobby.feature.notifications.platform.providePushTokenProvider
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val notificationsModule = module {

    single<PushTokenProvider> { providePushTokenProvider() }

    single<DeviceTokenApi> {
        val client: HttpClient = get(named("authedClient"))
        DeviceTokenApiImpl(client)
    }

    single<DeviceTokenRepository> {
        DeviceTokenRepositoryImpl(api = get(), tokenProvider = get())
    }

    factoryOf(::RegisterDeviceTokenUseCase)
    factoryOf(::UnregisterDeviceTokenUseCase)
}
