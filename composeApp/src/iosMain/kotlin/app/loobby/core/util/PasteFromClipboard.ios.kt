package app.loobby.core.util

import androidx.compose.runtime.Composable
import platform.UIKit.UIPasteboard

@Composable
actual fun rememberPasteFromClipboard(): () -> String? {
    return { UIPasteboard.generalPasteboard.string }
}
