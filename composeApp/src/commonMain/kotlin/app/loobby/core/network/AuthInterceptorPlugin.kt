package app.loobby.core.network

import app.loobby.core.storage.TokenStorage
import app.loobby.feature.auth.data.model.RefreshTokenRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpClientPlugin
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.plugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.util.AttributeKey
import app.loobby.feature.auth.data.model.AuthResponse
import io.ktor.http.encodedPath
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Plugin do Ktor HttpClient que:
 * 1. Injeta automaticamente o header `Authorization: Bearer <accessToken>` em toda request
 * 2. Ao receber 401 (Unauthorized), tenta refresh do token e repete a request
 *
 * Rotas públicas (/auth/anonymous, /auth/login, /auth/refresh) podem ser chamadas
 * sem token — o plugin simplesmente não encontra token no storage e segue sem injetar,
 * ou injeta o token do anônimo (o que o backend aceita normalmente via permitAll).
 *
 * A rota /auth/register exige o token do anônimo no header, que é injetado automaticamente.
 *
 * Uso:
 * ```
 * val client = HttpClient {
 *     install(AuthInterceptorPlugin) {
 *         tokenStorage = get<TokenStorage>()
 *         baseUrl = "https://api.loobby.app"
 *     }
 * }
 * ```
 */
class AuthInterceptorPlugin internal constructor(
    private val tokenStorage: TokenStorage,
    private val baseUrl: String
) {
    class Config {
        lateinit var tokenStorage: TokenStorage
        lateinit var baseUrl: String
    }

    companion object Plugin : HttpClientPlugin<Config, AuthInterceptorPlugin> {
        override val key = AttributeKey<AuthInterceptorPlugin>("AuthInterceptorPlugin")

        override fun prepare(block: Config.() -> Unit): AuthInterceptorPlugin {
            val config = Config().apply(block)
            return AuthInterceptorPlugin(config.tokenStorage, config.baseUrl)
        }

        override fun install(plugin: AuthInterceptorPlugin, scope: HttpClient) {
            val refreshMutex = Mutex()

            scope.plugin(HttpSend).intercept { request ->

                // 1) Injeta o token no header (se disponível)
                plugin.injectToken(request)

                // 2) Executa a request
                val originalCall = execute(request)

                // 3) Se deu 401 e não é uma rota de refresh (evita loop), tenta refresh
                if (originalCall.response.status == HttpStatusCode.Unauthorized
                    && !request.url.encodedPath.endsWith("/auth/refresh")
                    && !request.url.encodedPath.endsWith("/auth/anonymous")
                ) {
                    // Mutex garante que múltiplas requests 401 simultâneas
                    // façam apenas um refresh
                    val refreshed = refreshMutex.withLock {
                        plugin.tryRefreshToken(scope)
                    }

                    if (refreshed) {
                        // Re-injeta o novo token e repete a request
                        plugin.injectToken(request)
                        execute(request)
                    } else {
                        originalCall
                    }
                } else {
                    originalCall
                }
            }
        }
    }

    private suspend fun injectToken(request: HttpRequestBuilder) {
        val tokens = tokenStorage.getTokens()
        if (tokens != null) {
            request.header("Authorization", "Bearer ${tokens.accessToken}")
        }
    }

    /**
     * Tenta fazer refresh do token.
     * Retorna true se o refresh foi bem-sucedido e os novos tokens foram salvos.
     */
    private suspend fun tryRefreshToken(client: HttpClient): Boolean {
        return try {
            val tokens = tokenStorage.getTokens() ?: return false

            val response: AuthResponse = client.post("$baseUrl/auth/refresh") {
                contentType(ContentType.Application.Json)
                setBody(RefreshTokenRequest(tokens.refreshToken))
            }.body()

            // Atualiza os tokens no storage
            val updatedTokens = tokens.copy(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken,
                userId = response.userId,
                username = response.username,
                roles = response.roles
            )
            tokenStorage.saveTokens(updatedTokens)
            true
        } catch (_: Exception) {
            false
        }
    }
}