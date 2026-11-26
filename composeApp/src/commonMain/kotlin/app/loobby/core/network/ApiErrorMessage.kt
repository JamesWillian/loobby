package app.loobby.core.network

import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * A mensagem para mostrar à pessoa quando algo falhou. A v2 da API já responde em
 * português, pronta para a tela; o app só escolhe a mais específica. Erro do
 * servidor, queda de rede e bug do app viram textos fixos, nunca o texto cru da
 * exceção.
 */
fun Throwable.toUserMessage(fallback: String): String = when (this) {
    is CancellationException -> fallback
    is OfflineException -> message ?: fallback
    is ResponseException -> ApiErrorMessage.from(response.status.value, message) ?: fallback
    is IOException -> ApiErrorMessage.NO_CONNECTION
    // Resposta que o app não soube ler: é bug, e o texto dela não serve para a pessoa.
    is SerializationException -> fallback
    // As regras do domínio (require) já falam com a pessoa: "Escreva o comentário".
    is IllegalArgumentException -> message ?: fallback
    else -> fallback
}

object ApiErrorMessage {

    const val NO_CONNECTION = "Sem conexão com a internet. Confira a rede e tente de novo."
    const val SERVER_ERROR = "O servidor não respondeu como devia. Tente de novo daqui a pouco."
    private const val VALIDATION = "Dados inválidos"

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    @Serializable
    private data class Envelope(
        val message: String? = null,
        val fieldErrors: List<FieldError>? = null,
    )

    @Serializable
    private data class FieldError(val field: String? = null, val message: String? = null)

    /**
     * A mensagem do envelope de erro, ou null se não houver uma que sirva. Na
     * validação ("Dados inválidos"), vale a do primeiro campo, que diz o que corrigir.
     * [rawText] é o texto da exceção do cliente, que traz o corpo da resposta.
     */
    fun from(status: Int, rawText: String?): String? {
        if (status >= 500) return SERVER_ERROR
        val envelope = rawText?.let(::parseEnvelope) ?: return null
        val fieldMessage = envelope.fieldErrors.orEmpty().firstNotNullOfOrNull { it.message?.takeIf(String::isNotBlank) }
        val message = envelope.message?.takeIf(String::isNotBlank)
        return when {
            fieldMessage != null && (message == null || message == VALIDATION) -> fieldMessage
            else -> message
        }
    }

    /** O corpo JSON vem no meio do texto da exceção: do primeiro "{" ao último "}". */
    private fun parseEnvelope(rawText: String): Envelope? {
        val start = rawText.indexOf('{')
        val end = rawText.lastIndexOf('}')
        if (start < 0 || end <= start) return null
        return runCatching { json.decodeFromString<Envelope>(rawText.substring(start, end + 1)) }.getOrNull()
    }
}
