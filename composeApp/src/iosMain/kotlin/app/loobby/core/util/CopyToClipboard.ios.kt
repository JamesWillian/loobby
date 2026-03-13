package app.loobby.core.util

import androidx.compose.runtime.Composable
import platform.UIKit.UIPasteboard

@Composable
actual fun rememberCopyToClipboard(): (text: String) -> Unit {
    return { text ->
        UIPasteboard.generalPasteboard.string = text
    }
}