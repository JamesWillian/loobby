package app.loobby

import androidx.compose.ui.window.ComposeUIViewController
import app.loobby.core.di.iosPlatformModule

fun MainViewController() = ComposeUIViewController {
    initKoin(extraModules = listOf(iosPlatformModule))
    return@ComposeUIViewController App()
}