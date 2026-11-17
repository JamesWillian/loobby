package app.loobby.core.analytics

import app.loobby.core.network.OfflineException
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException

/**
 * Registra falhas que o app tratou sem fechar. Os crashes de verdade o Crashlytics
 * já captura sozinho.
 */
fun interface CrashReporter {
    fun report(error: Throwable)
}

/**
 * Registra [error] só quando ele não devia ter acontecido: um bug do app ou uma
 * resposta que o app não soube ler. Ficam de fora as falhas que a pessoa ou a rede
 * causam: ação cancelada, aparelho sem conexão, queda de rede e resposta de erro da
 * API (senha errada, código de convite inválido, servidor fora do ar).
 */
fun CrashReporter.reportIfUnexpected(error: Throwable) {
    when (error) {
        is CancellationException,
        is OfflineException,
        is IOException,
        is ResponseException -> Unit

        else -> report(error)
    }
}
