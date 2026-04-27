package app.loobby.core.network

import app.loobby.core.storage.TokenStorage
import app.loobby.feature.auth.data.model.AuthResponse
import app.loobby.feature.auth.data.model.RefreshTokenRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.plugin
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

object HttpClientFactory {

    fun createBaseClient(engine: HttpClientEngine): HttpClient =
        HttpClient(engine) {
            expectSuccess = true

            defaultRequest {
                url(NetworkConfig.BASE_URL)
                contentType(ContentType.Application.Json)
            }

            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        encodeDefaults = true
                    }
                )
            }

            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        println("Ktor -> $message")
                    }
                }
//                level = LogLevel.INFO
                level = LogLevel.NONE
            }
        }

    /**
     * Client para rotas autenticadas.
     *
     * 1. Injeta Authorization: Bearer <accessToken> em toda request
     * 2. Se receber 401, faz refresh usando o baseClient (sem token no header)
     * 3. Salva novos tokens e repete a request original
     *
     * O baseClient é usado para o refresh para evitar que o token expirado
     * seja enviado no header — o Spring Security rejeita JWT expirado
     * mesmo em rotas permitAll().
     */
    fun createAuthedClient(
        engine: HttpClientEngine,
        tokenStorage: TokenStorage
    ): HttpClient {

        // baseClient para chamadas de refresh (sem token no header)
        val refreshClient = createBaseClient(engine)
        val refreshMutex = Mutex()

        val client = HttpClient(engine) {
            expectSuccess = false  // não lançar exceção em 401, tratar manualmente

            defaultRequest {
                url(NetworkConfig.BASE_URL)
                contentType(ContentType.Application.Json)
            }

            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        encodeDefaults = true
                    }
                )
            }

            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        println("Ktor Auth -> $message")
                    }
                }
//                level = LogLevel.INFO
                level = LogLevel.NONE
            }
        }

        // Interceptor: injeta token + auto-refresh em 401
        client.plugin(HttpSend).intercept { request ->

            // 1) Injeta o token atual no header
            tokenStorage.getTokens()?.accessToken?.let { token ->
                request.headers.remove("Authorization")
                request.header("Authorization", "Bearer $token")
            }

            // 2) Executa a request
            val originalCall = execute(request)

            // 3) Se 401, tenta refresh e repete
            val finalCall = if (originalCall.response.status == HttpStatusCode.Unauthorized) {
                val refreshed = refreshMutex.withLock {
                    tryRefreshToken(refreshClient, tokenStorage)
                }
                if (refreshed) {
                    tokenStorage.getTokens()?.accessToken?.let { newToken ->
                        request.headers.remove("Authorization")
                        request.header("Authorization", "Bearer $newToken")
                    }
                    execute(request)
                } else {
                    originalCall
                }
            } else {
                originalCall
            }

            // lança exceção para qualquer status não-2xx (exceto 401 já tratado)
            val status = finalCall.response.status
            if (status.value !in 200..299) {
                val body = finalCall.response.bodyAsText()
                throw io.ktor.client.plugins.ClientRequestException(
                    finalCall.response,
                    "HTTP ${status.value}: $body"
                )
            }

            finalCall
        }

        return client
    }

    /**
     * Faz refresh usando o baseClient (sem Authorization header).
     * Retorna true se o refresh foi bem-sucedido.
     */
    private suspend fun tryRefreshToken(
        refreshClient: HttpClient,
        tokenStorage: TokenStorage
    ): Boolean {
        return try {
            val tokens = tokenStorage.getTokens() ?: return false

            val response: AuthResponse = refreshClient.post("/auth/refresh") {
                contentType(ContentType.Application.Json)
                setBody(RefreshTokenRequest(tokens.refreshToken))
            }.body()

            val updatedTokens = tokens.copy(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken,
                userId = response.userId,
                username = response.username,
                roles = response.roles
            )
            tokenStorage.saveTokens(updatedTokens)
            true
        } catch (e: Exception) {
            println("Ktor Auth -> Refresh failed: ${e.message}")
            false
        }
    }
}