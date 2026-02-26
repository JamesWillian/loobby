package app.loobby.feature.auth.di

import app.loobby.feature.auth.data.remote.AuthApi
import app.loobby.feature.auth.data.remote.AuthApiImpl
import app.loobby.feature.auth.domain.repository.AuthRepository
import app.loobby.feature.auth.domain.repository.AuthRepositoryImpl
import app.loobby.feature.auth.domain.usecase.GetProfileUseCase
import app.loobby.feature.auth.domain.usecase.InitializeAnonymousUseCase
import app.loobby.feature.auth.domain.usecase.LoginUseCase
import app.loobby.feature.auth.domain.usecase.RegisterUseCase
import app.loobby.feature.auth.domain.usecase.UpdateProfileUseCase
import app.loobby.feature.auth.domain.usecase.UploadAvatarUseCase
import app.loobby.feature.auth.presentation.AuthViewModel
import org.koin.dsl.module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named

val authModule = module {

    single<AuthApi> {
        // usa o baseClient
        val client = get<io.ktor.client.HttpClient>(qualifier = named("baseClient"))
        AuthApiImpl(client)
    }

    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }

    factoryOf(::InitializeAnonymousUseCase)
    factoryOf(::LoginUseCase)
    factoryOf(::RegisterUseCase)
    factoryOf(::GetProfileUseCase)
    factoryOf(::UpdateProfileUseCase)
    factoryOf(::UploadAvatarUseCase)

    // como não usamos androidx ViewModel, apenas cria como single/factory
    single {
        AuthViewModel(
            initializeAnonymousUseCase = get(),
            loginUseCase = get(),
            registerUseCase = get(),
            getProfileUseCase = get(),
            updateProfileUseCase = get(),
            uploadAvatarUseCase = get(),
            repo = get()
        )
    }
}