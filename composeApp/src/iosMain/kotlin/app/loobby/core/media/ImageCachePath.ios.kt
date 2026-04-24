package app.loobby.core.media

import coil3.PlatformContext
import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

/**
 * No iOS usamos `NSCachesDirectory` (~/Library/Caches) — o SO pode
 * limpar sob pressão de armazenamento, igual ao cacheDir do Android,
 * que é exatamente o comportamento esperado de um cache descartável.
 *
 * Fallback para `/tmp` apenas se a busca falhar (extremamente improvável
 * em iOS real — só ocorre em ambientes desconfigurados).
 */
actual fun imageDiskCacheDir(context: PlatformContext): Path {
    val cachesDir = NSSearchPathForDirectoriesInDomains(
        directory = NSCachesDirectory,
        domainMask = NSUserDomainMask,
        expandTilde = true
    ).firstOrNull() as? String
    val base = cachesDir ?: "/tmp"
    return "$base/loobby_image_cache".toPath()
}
