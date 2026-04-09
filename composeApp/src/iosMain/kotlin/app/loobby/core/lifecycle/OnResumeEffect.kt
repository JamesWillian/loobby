package app.loobby.core.lifecycle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

@Composable
actual fun OnResumeEffect(onResume: () -> Unit) {
    DisposableEffect(Unit) {
        val observer = platform.Foundation.NSNotificationCenter.defaultCenter
            .addObserverForName(
                name = platform.UIKit.UIApplicationDidBecomeActiveNotification,
                `object` = null,
                queue = null
            ) { _ ->
                onResume()
            }
        onDispose {
            platform.Foundation.NSNotificationCenter.defaultCenter
                .removeObserver(observer)
        }
    }
}