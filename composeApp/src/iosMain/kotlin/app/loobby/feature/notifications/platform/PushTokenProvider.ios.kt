package app.loobby.feature.notifications.platform

/**
 * iOS: implementação stub para o MVP.
 *
 * Para ativar push no iOS, será necessário:
 *  1. Adicionar FirebaseCore + FirebaseMessaging via SPM no target iosApp
 *  2. No AppDelegate.swift:
 *      - FirebaseApp.configure()
 *      - UIApplication.shared.registerForRemoteNotifications()
 *      - Messaging.messaging().delegate = self
 *      - Implementar messaging(_, didReceiveRegistrationToken:) e expor via callback para Kotlin
 *  3. Trocar esta implementação por uma que recebe o token via bridge
 *     (por exemplo, com uma classe `IosPushTokenBridge` atualizada pelo Swift).
 *
 * Hoje retorna null — o app funciona normalmente, apenas sem push no iOS.
 */
private class IosPushTokenProvider : PushTokenProvider {
    override val platform: PushPlatform = PushPlatform.IOS
    override suspend fun getToken(): String? = null
}

actual fun providePushTokenProvider(): PushTokenProvider = IosPushTokenProvider()
