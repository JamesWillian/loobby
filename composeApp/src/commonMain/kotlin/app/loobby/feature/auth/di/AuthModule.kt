package app.loobby.feature.auth.di

import app.loobby.feature.auth.data.remote.AuthApi
import app.loobby.feature.auth.data.remote.AuthApiImpl
import app.loobby.feature.auth.domain.repository.AuthRepository
import app.loobby.feature.auth.domain.repository.AuthRepositoryImpl
import app.loobby.feature.auth.domain.usecase.GetProfileUseCase
import app.loobby.feature.auth.domain.usecase.InitializeAnonymousUseCase
import app.loobby.feature.auth.domain.usecase.IsAnonymousUseCase
import app.loobby.feature.auth.domain.usecase.LoginUseCase
import app.loobby.feature.auth.domain.usecase.RegisterUseCase
import app.loobby.feature.auth.domain.usecase.UpdateProfileUseCase
import app.loobby.feature.auth.domain.usecase.UploadAvatarUseCase
import app.loobby.feature.auth.presentation.AuthViewModel
import io.ktor.client.HttpClient
import org.koin.dsl.module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named

val authModule = module {

// AuthApi usa o authedClient porque /auth/register exige Bearer token.
    // As rotas públicas (/auth/anonymous, /auth/login, /auth/refresh) aceitam
    // o token sem problemas (SecurityConfig: permitAll não rejeita token extra).
    single<AuthApi> {
        val client: HttpClient = get(named("authedClient"))
        AuthApiImpl(client)
    }

    single<AuthRepository> { AuthRepositoryImpl(api = get(), tokenStorage = get()) }

    factoryOf(::InitializeAnonymousUseCase)
    factoryOf(::IsAnonymousUseCase)
    factoryOf(::LoginUseCase)
    factoryOf(::RegisterUseCase)
    factoryOf(::GetProfileUseCase)
    factoryOf(::UpdateProfileUseCase)
    factoryOf(::UploadAvatarUseCase)

    // como não usamos androidx ViewModel, apenas cria como single/factory
    single {
        AuthViewModel(
            initializeAnonymousUseCase = get(),
            isAnonymousUseCase = get(),
            loginUseCase = get(),
            registerUseCase = get(),
            getProfileUseCase = get(),
            updateProfileUseCase = get(),
            uploadAvatarUseCase = get()
        )
    }
}