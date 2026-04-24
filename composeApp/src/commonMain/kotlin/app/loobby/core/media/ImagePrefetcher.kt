package app.loobby.core.media

import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Dispara um "warming" do disk cache do Coil para uma lista de URLs.
 *
 * Uso: toda vez que o app recebe uma listagem do servidor (feed, grupos,
 * eventos, membros), chamamos [prefetch] com as URLs de imagens do payload
 * para garantir que elas estejam disponíveis quando o usuário abrir cada
 * item — inclusive se ficar offline antes de visualizá-los.
 *
 * Decisões importantes:
 *  - `memoryCachePolicy = WRITE_ONLY`: escreve no memory cache mas não lê
 *    — não queremos que o prefetch influencie o LRU do que está na tela.
 *  - `diskCachePolicy = ENABLED`: prioridade é persistir no disco.
 *  - Requests são "fire-and-forget" via `enqueue` — se falharem, tudo bem,
 *    o Coil tentará de novo quando a imagem aparecer na UI.
 *  - Deduplicação é responsabilidade do Coil (mesmo URL em vôo = 1 request).
 *
 * Este prefetcher usa [SingletonImageLoader.get] para reaproveitar o mesmo
 * ImageLoader configurado em [app.loobby.App] — assim memory/disk cache e
 * mapeamento de URL relativa são compartilhados com os `AsyncImage` da UI.
 */
class ImagePrefetcher(
    private val context: PlatformContext
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /**
     * Pré-carrega as URLs informadas. Valores nulos ou em branco são
     * ignorados silenciosamente. Chamadas repetidas com as mesmas URLs
     * são baratas (Coil deduplica requests em vôo e não refaz download
     * de imagens já presentes no disk cache).
     */
    fun prefetch(urls: List<String?>) {
        val targets = urls
            .asSequence()
            .filterNotNull()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()

        if (targets.isEmpty()) return

        scope.launch {
            val loader = SingletonImageLoader.get(context)
            for (url in targets) {
                val request = ImageRequest.Builder(context)
                    .data(url)
                    .memoryCachePolicy(CachePolicy.WRITE_ONLY)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build()
                loader.enqueue(request)
            }
        }
    }

    /** Sobrecarga para um único URL — açúcar para call-sites simples. */
    fun prefetch(url: String?) = prefetch(listOf(url))
}
