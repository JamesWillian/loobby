package app.loobby.core.network

import app.loobby.core.storage.TokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
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
                level = LogLevel.INFO
            }
        }

    /**
     * Client padrão para rotas autenticadas.
     * Apenas injeta o Authorization se existir accessToken.
     * Lógica de refresh fica no repositório.
     */
    fun createAuthedClient(
        engine: HttpClientEngine,
        tokenStorage: TokenStorage
    ): HttpClient =
        HttpClient(engine) {
            expectSuccess = true

            defaultRequest {
                url(NetworkConfig.BASE_URL)
                contentType(ContentType.Application.Json)

                tokenStorage.getTokens()?.accessToken?.let { token ->
                    headers.append("Authorization", "Bearer $token")
                }
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
                level = LogLevel.INFO
            }
        }
}