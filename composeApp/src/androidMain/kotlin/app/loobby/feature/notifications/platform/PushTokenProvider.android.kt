package app.loobby.feature.notifications.platform

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

private class AndroidPushTokenProvider : PushTokenProvider {

    override val platform: PushPlatform = PushPlatform.ANDROID

    override suspend fun getToken(): String? =
        suspendCancellableCoroutine { cont ->
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token -> cont.resume(token) }
                .addOnFailureListener { _ -> cont.resume(null) }
                .addOnCanceledListener { cont.resume(null) }
        }
}

actual fun providePushTokenProvider(): PushTokenProvider = AndroidPushTokenProvider()
