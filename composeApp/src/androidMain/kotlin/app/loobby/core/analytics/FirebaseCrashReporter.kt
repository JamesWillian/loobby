package app.loobby.core.analytics

import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics

/**
 * Registra no Crashlytics, como não fatais, as falhas que o app tratou. No debug, a
 * coleta fica desligada pelo manifesto.
 */
class FirebaseCrashReporter : CrashReporter {

    override fun report(error: Throwable) {
        Firebase.crashlytics.recordException(error.withoutMessage())
    }
}

/**
 * Cópia da falha só com o tipo e a pilha de chamadas, dela e das causas. A mensagem
 * fica no aparelho: a de um erro ao ler a resposta da API pode trazer um trecho do
 * JSON, com o nome ou o e-mail de alguém.
 */
private fun Throwable.withoutMessage(): Throwable {
    val copy = HandledFailure(
        type = javaClass.name,
        cause = cause?.takeIf { it !== this }?.withoutMessage()
    )
    copy.stackTrace = stackTrace
    return copy
}

private class HandledFailure(type: String, cause: Throwable?) : Exception(type, cause)
