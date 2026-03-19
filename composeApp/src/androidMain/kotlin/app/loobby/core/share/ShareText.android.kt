package app.loobby.core.share

import android.content.Context
import android.content.Intent
import app.loobby.LoobbyApplication  // substitua pelo seu Application/Context holder

actual fun shareText(text: String) {
    val context: Context = LoobbyApplication.appContext
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(Intent.createChooser(intent, null).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    })
}