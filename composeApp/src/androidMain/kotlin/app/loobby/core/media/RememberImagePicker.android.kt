package app.loobby.core.media

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CompletableDeferred

@Composable
actual fun rememberImagePicker(): ImagePicker {
    val context = LocalContext.current

    // Vamos “entregar” o resultado via CompletableDeferred para o suspend fun
    var pendingResult by remember { mutableStateOf<CompletableDeferred<PickedImage?>?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val deferred = pendingResult
        pendingResult = null

        if (deferred == null) return@rememberLauncherForActivityResult

        if (uri == null) {
            deferred.complete(null)
            return@rememberLauncherForActivityResult
        }

        try {
            val bytes = readBytesFromUri(context, uri)
            val contentType = context.contentResolver.getType(uri) ?: "image/*"
            val ext = contentType.substringAfterLast('/', "png")
            val fileName = "avatar.$ext"

            deferred.complete(
                PickedImage(
                    fileName = fileName,
                    bytes = bytes,
                    contentType = contentType
                )
            )
        } catch (t: Throwable) {
            deferred.completeExceptionally(t)
        }
    }

    return remember {
        object : ImagePicker {
            override suspend fun pickImage(): PickedImage? {
                val deferred = CompletableDeferred<PickedImage?>()
                pendingResult = deferred
                launcher.launch("image/*")
                return deferred.await()
            }
        }
    }
}

private fun readBytesFromUri(context: Context, uri: Uri): ByteArray {
    return context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        ?: error("Could not read selected file")
}