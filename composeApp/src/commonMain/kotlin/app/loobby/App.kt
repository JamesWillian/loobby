package app.loobby

import androidx.compose.runtime.*
import app.loobby.core.di.coreModule
import app.loobby.core.media.RelativeUrlMapper
import app.loobby.core.network.NetworkConfig.BASE_URL
import app.loobby.feature.auth.di.authModule
import app.loobby.feature.events.di.eventsModule
import app.loobby.feature.events.teams.di.teamsModule
import app.loobby.feature.groups.di.groupsModule
import app.loobby.feature.groups.ui.AppShell
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.compose.setSingletonImageLoaderFactory
import org.koin.core.context.startKoin
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.mp.KoinPlatformTools

fun initKoin(extraModules: List<Module> = emptyList()): KoinApplication? {
    if (KoinPlatformTools.defaultContext().getOrNull() != null) return null
    return startKoin {
        modules(
            coreModule,
            authModule,
            groupsModule,
            eventsModule,
            teamsModule,
            *extraModules.toTypedArray()
        )
    }
}

@Composable
fun App() {
    setSingletonImageLoaderFactory { context: PlatformContext ->
        ImageLoader.Builder(context)
            .components {
                add(RelativeUrlMapper(BASE_URL))
            }
            .build()
    }
    AppShell()
}