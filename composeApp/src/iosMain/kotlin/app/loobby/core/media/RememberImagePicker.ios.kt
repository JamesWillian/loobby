package app.loobby.core.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CompletableDeferred
import platform.Foundation.NSData
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UniformTypeIdentifiers.UTTypeImage
import platform.darwin.NSObject

@Composable
actual fun rememberImagePicker(): ImagePicker {
    return remember { IosImagePicker() }
}

private class IosImagePicker : ImagePicker {

    // Referência forte ao delegate — o picker.delegate é weak em Obj-C,
    // sem isso o delegate pode ser coletado pelo GC antes do callback
    private var activeDelegate: PickerDelegate? = null

    override suspend fun pickImage(): PickedImage? {
        val deferred = CompletableDeferred<PickedImage?>()

        val config = PHPickerConfiguration().apply {
            selectionLimit = 1
            filter = PHPickerFilter.imagesFilter
        }

        val delegate = PickerDelegate(deferred) { activeDelegate = null }
        activeDelegate = delegate // strong ref
        val picker = PHPickerViewController(configuration = config)
        picker.delegate = delegate

        val rootVc = UIApplication.sharedApplication.connectedScenes
            .filterIsInstance<UIWindowScene>()
            .flatMap { it.windows.filterIsInstance<UIWindow>() }
            .firstOrNull { it.isKeyWindow() }
            ?.rootViewController
            ?: run {
                deferred.complete(null)
                return null
            }

        // Pega o VC que está no topo (pode estar apresentando outro sheet)
        var topVc = rootVc
        while (topVc.presentedViewController != null) {
            topVc = topVc.presentedViewController!!
        }

        // Apresentar o picker na main thread
        topVc.presentViewController(picker, animated = true, completion = null)

        return deferred.await()
    }
}

private class PickerDelegate(
    private val deferred: CompletableDeferred<PickedImage?>,
    private val onFinished: () -> Unit
) : NSObject(), PHPickerViewControllerDelegateProtocol {

    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        picker.dismissViewControllerAnimated(true, completion = null)

        val results = didFinishPicking.filterIsInstance<PHPickerResult>()
        val firstResult = results.firstOrNull()

        if (firstResult == null) {
            deferred.complete(null)
            onFinished()
            return
        }

        val provider = firstResult.itemProvider
        val typeId = UTTypeImage.identifier

        if (!provider.hasItemConformingToTypeIdentifier(typeId)) {
            deferred.complete(null)
            onFinished()
            return
        }

        provider.loadDataRepresentationForTypeIdentifier(typeId) { nsData, error ->
            if (error != null || nsData == null) {
                deferred.complete(null)
                onFinished()
                return@loadDataRepresentationForTypeIdentifier
            }

            try {
                val bytes = nsData.toByteArray()
                val contentType = provider.registeredTypeIdentifiers.firstOrNull()
                    ?.toString() ?: "image/jpeg"

                val ext = when {
                    contentType.contains("png") -> "png"
                    contentType.contains("webp") -> "webp"
                    contentType.contains("gif") -> "gif"
                    else -> "jpg"
                }

                deferred.complete(
                    PickedImage(
                        fileName = "avatar.$ext",
                        bytes = bytes,
                        contentType = "image/$ext"
                    )
                )
            } catch (t: Throwable) {
                deferred.complete(null)
            }
            onFinished()
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val length = this.length.toInt()
    if (length == 0) return ByteArray(0)
    val result = ByteArray(length)
    result.usePinned { pinned ->
        platform.posix.memcpy(
            pinned.addressOf(0),
            this.bytes,
            this.length
        )
    }
    return result
}