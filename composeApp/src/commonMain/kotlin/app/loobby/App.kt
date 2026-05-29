package app.loobby

import androidx.compose.runtime.*
import app.loobby.core.di.coreModule
import app.loobby.core.media.RelativeUrlMapper
import app.loobby.core.media.imageDiskCacheDir
import app.loobby.core.network.NetworkConfig.BASE_URL
import app.loobby.feature.auth.di.authModule
import app.loobby.feature.events.di.eventsModule
import app.loobby.feature.events.teams.di.teamsModule
import app.loobby.feature.games.di.gamesModule
import app.loobby.feature.groups.di.groupsModule
import app.loobby.feature.groups.ui.AppShell
import app.loobby.feature.notifications.di.notificationsModule
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.compose.setSingletonImageLoaderFactory
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import coil3.request.crossfade
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
            gamesModule,
            notificationsModule,
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
            // Memory cache — acelera rerenders da mesma imagem dentro da sessão.
            // 25% da memória livre reportada pelo SO é um trade-off saudável.
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.25)
                    .build()
            }
            // Disk cache — base do modo offline. As imagens ficam persistidas
            // endereçadas por URL; quando o app está sem rede, o Coil resolve
            // o binário diretamente do disco. O diretório é descartável (o SO
            // pode limpar sob pressão de armazenamento).
            .diskCache {
                DiskCache.Builder()
                    .directory(imageDiskCacheDir(context))
                    .maxSizeBytes(200L * 1024 * 1024) // 200 MB
                    .build()
            }
            // Políticas explícitas — valem o mesmo que o default atual do Coil,
            // mas deixam a intenção documentada: ler e escrever em ambos os
            // caches para que as imagens carregadas online fiquem disponíveis
            // offline posteriormente.
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .crossfade(true)
            .build()
    }
    AppShell()
}