package app.loobby.feature.notifications.platform

/**
 * Plataforma-específica: obtém o token de push do device.
 *  - Android: FirebaseMessaging.getInstance().token (FCM)
 *  - iOS: por enquanto retorna null (requer setup do Firebase iOS via SPM/CocoaPods)
 */
interface PushTokenProvider {

    /** Plataforma (ANDROID / IOS). Valor usado no payload `/devices/register`. */
    val platform: PushPlatform

    /**
     * Solicita o token de push do device. Pode retornar null se:
     *  - permissão de notificação negada (Android 13+)
     *  - Firebase não inicializado
     *  - plataforma ainda não suportada (iOS no MVP)
     */
    suspend fun getToken(): String?
}

enum class PushPlatform { ANDROID, IOS }

/**
 * Fornecido pelas fontes-platform-específicas (expect/actual via Koin).
 * Usamos uma função factory que o módulo Koin chama em `androidPlatformModule`
 * e `iosPlatformModule` para evitar expect/actual de classe (que complica compose-kmp).
 */
expect fun providePushTokenProvider(): PushTokenProvider
