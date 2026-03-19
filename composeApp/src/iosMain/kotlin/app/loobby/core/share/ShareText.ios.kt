package app.loobby.core.share

import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

actual fun shareText(text: String) {
    val activityVC = UIActivityViewController(
        activityItems = listOf(text),
        applicationActivities = null
    )
    UIApplication.sharedApplication.keyWindow
        ?.rootViewController
        ?.presentViewController(activityVC, animated = true, completion = null)
}