package app.loobby.core.db

import kotlinx.serialization.json.Json

/**
 * Instância de Json dedicada ao cache local (SQLDelight).
 *  - `ignoreUnknownKeys`: protege contra quebras quando evoluímos os modelos.
 *  - `encodeDefaults`: garante que campos opcionais com default sejam escritos,
 *    para o cache não "esquecer" valores explicitos já materializados.
 *
 * Fica separado do Json usado pelo Ktor (HttpClientFactory) para não acoplar
 * a forma "wire" da API com a forma "wire" do cache — evoluem em tempos
 * diferentes.
 */
val CacheJson: Json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}
