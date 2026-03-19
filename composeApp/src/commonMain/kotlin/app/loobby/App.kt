package app.loobby

import androidx.compose.runtime.*
import app.loobby.core.di.coreModule
import app.loobby.feature.auth.di.authModule
import app.loobby.feature.events.di.eventsModule
import app.loobby.feature.groups.di.groupsModule
import app.loobby.feature.groups.ui.AppShell
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
    AppShell()
}