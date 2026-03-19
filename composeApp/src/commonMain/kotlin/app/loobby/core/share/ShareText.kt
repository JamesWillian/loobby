package app.loobby.core.share

/**
 * Abre o seletor de compartilhamento nativo da plataforma com [text].
 * Implementado via expect/actual para Android e iOS.
 */
expect fun shareText(text: String)