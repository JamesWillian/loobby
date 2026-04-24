package app.loobby.core.media

import coil3.PlatformContext
import okio.Path
import okio.Path.Companion.toOkioPath

/**
 * No Android, `PlatformContext` é um alias para `android.content.Context`.
 * Usamos `cacheDir` porque é específico do app e o SO pode limpar quando
 * o armazenamento fica crítico (o disk cache de imagens é descartável).
 */
actual fun imageDiskCacheDir(context: PlatformContext): Path =
    context.cacheDir.resolve("loobby_image_cache").toOkioPath()
