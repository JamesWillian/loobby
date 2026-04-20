package app.loobby

import androidx.compose.ui.window.ComposeUIViewController
import app.loobby.core.di.iosPlatformModule
import app.loobby.feature.auth.domain.GoogleSignInProvider
import org.koin.dsl.module

fun MainViewController(
    googleSignInProvider: GoogleSignInProvider
) = ComposeUIViewController {
    initKoin(
        extraModules = listOf(
            iosPlatformModule,
            module {
                single<GoogleSignInProvider> { googleSignInProvider }
            }
        )
    )
    return@ComposeUIViewController App()
}