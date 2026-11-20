package app.loobby.core.appconfig

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText

/**
 * Usa o baseClient, sem Authorization: a rota é pública, e a API recusa com 401 até rota
 * pública quando o token enviado está vencido — o access token dura 15 minutos, então
 * quase toda abertura do app teria um vencido no header.
 */
class AppConfigApiImpl(
    private val client: HttpClient,
) : AppConfigApi {

    override suspend fun fetchConfig(): String =
        client.get("app/config").bodyAsText()
}
