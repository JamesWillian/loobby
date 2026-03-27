package app.loobby.core.media

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.Image as SkiaImage
import platform.CoreGraphics.CGBitmapContextCreate
import platform.CoreGraphics.CGBitmapContextCreateImage
import platform.CoreGraphics.CGColorSpaceCreateDeviceRGB
import platform.CoreGraphics.CGImageCreateWithImageInRect
import platform.CoreGraphics.CGImageGetHeight
import platform.CoreGraphics.CGImageGetWidth
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation

@OptIn(ExperimentalForeignApi::class)
actual object ImageProcessor {

    actual fun decode(bytes: ByteArray): ImageBitmap {
        return SkiaImage.makeFromEncoded(bytes).toComposeImageBitmap()
    }

    actual fun cropAndResize(
        bytes: ByteArray,
        cropX: Int,
        cropY: Int,
        cropSize: Int,
        targetSize: Int,
        quality: Int
    ): ByteArray {
        val nsData = bytes.usePinned { pinned ->
            NSData.create(
                bytes = pinned.addressOf(0),
                length = bytes.size.toULong()
            )
        }
        val uiImage = UIImage(data = nsData)
        val cgImage = uiImage.CGImage ?: error("Não foi possível obter CGImage")

        val imgWidth = CGImageGetWidth(cgImage).toInt()
        val imgHeight = CGImageGetHeight(cgImage).toInt()

        // ── Clamp ──
        val safeX = cropX.coerceIn(0, (imgWidth - 1).coerceAtLeast(0))
        val safeY = cropY.coerceIn(0, (imgHeight - 1).coerceAtLeast(0))
        val maxSize = minOf(cropSize, imgWidth - safeX, imgHeight - safeY)
            .coerceAtLeast(1)

        // ── Crop ──
        val cropRect = CGRectMake(
            safeX.toDouble(),
            safeY.toDouble(),
            maxSize.toDouble(),
            maxSize.toDouble()
        )
        val croppedCg = CGImageCreateWithImageInRect(cgImage, cropRect)
            ?: error("Falha ao recortar imagem")

        // ── Resize ──
        val colorSpace = CGColorSpaceCreateDeviceRGB()
        val context = CGBitmapContextCreate(
            data = null,
            width = targetSize.toULong(),
            height = targetSize.toULong(),
            bitsPerComponent = 8u,
            bytesPerRow = (targetSize * 4).toULong(),
            space = colorSpace,
            bitmapInfo = 1u // kCGImageAlphaPremultipliedLast = 1
        ) ?: error("Falha ao criar bitmap context")

        val drawRect = CGRectMake(0.0, 0.0, targetSize.toDouble(), targetSize.toDouble())
        platform.CoreGraphics.CGContextDrawImage(context, drawRect, croppedCg)

        val resizedCg = CGBitmapContextCreateImage(context)
            ?: error("Falha ao criar imagem redimensionada")

        val resizedUi = UIImage(cGImage = resizedCg)

        // ── Encode JPEG ──
        val qualityNormalized = quality.toDouble() / 100.0
        val jpegData = UIImageJPEGRepresentation(resizedUi, qualityNormalized)
            ?: error("Falha ao codificar JPEG")

        return jpegData.toByteArray()
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val length = this.length.toInt()
    val result = ByteArray(length)
    if (length > 0) {
        result.usePinned { pinned ->
            platform.posix.memcpy(
                pinned.addressOf(0),
                this.bytes,
                this.length
            )
        }
    }
    return result
}