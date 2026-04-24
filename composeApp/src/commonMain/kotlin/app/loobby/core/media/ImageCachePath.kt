package app.loobby.core.media

import coil3.PlatformContext
import okio.Path

/**
 * Diretório usado pelo Coil para persistir o disk cache de imagens.
 *
 * Cada plataforma decide um local apropriado:
 *   • Android: `context.cacheDir/loobby_image_cache` — limpo pelo SO sob
 *     pressão de armazenamento, mas sobrevive a restarts normais.
 *   • iOS:     `~/Library/Caches/loobby_image_cache` — idem.
 *
 * Os binários das imagens ficam endereçados pela URL (hash interno do Coil),
 * então não precisamos armazená-los no SQLDelight — basta guardar a URL na
 * entidade (Group.imageUrl, Event.confirmedAvatars, etc.) e o Coil resolve
 * o binário a partir do disco quando a imagem for exibida offline.
 */
expect fun imageDiskCacheDir(context: PlatformContext): Path
