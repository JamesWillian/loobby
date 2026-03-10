package app.loobby

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.loobby.core.di.coreModule
import app.loobby.core.navigation.RootRoute
import app.loobby.core.navigation.rememberRootNavigator
import app.loobby.feature.auth.di.authModule
import app.loobby.feature.auth.presentation.AuthScreen
import app.loobby.feature.events.di.eventsModule
import app.loobby.feature.groups.di.groupsModule
import app.loobby.feature.groups.ui.AppShell
import org.jetbrains.compose.resources.painterResource

import org.koin.core.context.startKoin
import org.koin.core.KoinApplication
import org.koin.core.module.Module

fun initKoin(extraModules: List<Module> = emptyList()): KoinApplication =
    startKoin {
        modules(
            coreModule,
            authModule,
            groupsModule,
            eventsModule,
            *extraModules.toTypedArray()
        )
    }

@Composable
fun App() {
    val rootNavigator = rememberRootNavigator()

    when (rootNavigator.current) {
        RootRoute.App -> AppShell(rootNavigator)
        RootRoute.Profile -> {}
    }
}